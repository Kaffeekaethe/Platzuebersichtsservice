package platzuebersichtservice.domainmodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class JdbcPlatzDAO implements PlatzDAO {

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void insert(Platz platz) {

		String sql = "INSERT INTO platz " + "(zug_id, platz_id, status) VALUES (?, ?, ?)";
		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, platz.getZugID());
			ps.setInt(2, platz.getPlatzID());
			ps.setInt(3, platz.getStatus());
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			throw new RuntimeException(e);

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public Platz findPlatzByZugUndPlatz(int zug_id, int platz_id) {

		String sql = "SELECT * FROM platz WHERE zug_id = ? AND platz_id = ?";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, zug_id);
			ps.setInt(2, platz_id);
			Platz platz = null;
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				platz = new Platz(rs.getInt("ZUG_ID"), rs.getInt("PLATZ_ID"), rs.getInt("STATUS"),
						rs.getBoolean("raucher"), rs.getBoolean("fenster"), rs.getInt("klasse"));
			}
			rs.close();
			ps.close();
			return platz;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public Platz findPlatzByID(int id) {

		String sql = "SELECT * FROM platz WHERE id = ?";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			Platz platz = null;
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				platz = new Platz(rs.getInt("ZUG_ID"), rs.getInt("PLATZ_ID"), rs.getInt("STATUS"),
						rs.getBoolean("raucher"), rs.getBoolean("fenster"), rs.getInt("klasse"));
			}
			rs.close();
			ps.close();
			return platz;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public List<Integer> getZuege(String start, String ziel) {
		// alle Züge holen, die einen Eintrag mit freiem Status haben
		String sql = "";
		Connection conn = null;
		sql = "SELECT DISTINCT * FROM (SELECT DISTINCT platz.zug_id AS zug_id FROM platz "
				+ "JOIN haltestelle where haltestelle.zug_id = platz.zug_id and haltestelle = ?) AS start "
				+ "JOIN (SELECT DISTINCT platz.zug_id AS zug_id FROM platz JOIN haltestelle "
				+ "WHERE haltestelle.zug_id = platz.zug_id and haltestelle = ?) as ziel "
				+ "WHERE start.zug_id = ziel.zug_id;";


		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			List<Integer> zug_ids = new ArrayList<Integer>();
			ps.setString(1, start);
			ps.setString(2, ziel);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				zug_ids.add(rs.getInt("ZUG_ID"));
			}
			rs.close();
			ps.close();
			return zug_ids;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public List<Platz> getPlaetze(int zug_id) {
		String sql = "SELECT * FROM platz WHERE zug_id = ? AND status = 0";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, zug_id);
			List<Platz> plaetze = new ArrayList<Platz>();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				plaetze.add(new Platz(rs.getInt("ZUG_ID"), rs.getInt("PLATZ_ID"), rs.getInt("STATUS"),
						rs.getBoolean("raucher"), rs.getBoolean("fenster"), rs.getInt("klasse")));
			}
			rs.close();
			ps.close();
			return plaetze;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public int getStatus(int zug_id, int platz_id, boolean lock) {
		String sql = "SELECT * FROM platz WHERE zug_id = ? AND platz_id = ?";
		int status = -1;
		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, zug_id);
			ps.setInt(2, platz_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				status = rs.getInt("STATUS");
				if (lock && status == 0) {
					sql = "UPDATE platz SET status = 1 WHERE id = ?";
					ps = conn.prepareStatement(sql);
					ps.setInt(1, rs.getInt("ID"));
					ps.executeUpdate();
					System.out.println("temporarly locked " + rs.getInt("ZUG_ID") + " " + rs.getInt("PLATZ_ID"));

					// Starte ein Thread zur Observation
					Observation observation = new Observation(rs.getInt("ID"));
					Thread t = new Thread(observation);
					t.start();
				}
			}
			rs.close();
			ps.close();
			return status;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

	}

	public void unlockPlatz(int zug_id, int platz_id) {
		String sql = "UPDATE platz SET status = 0 WHERE zug_id = ? AND platz_id = ?";
		PreparedStatement ps = null;
		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, zug_id);
			ps.setInt(2, platz_id);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public void lockPlatz(int zug_id, int platz_id) {
		String sql = "UPDATE platz SET status = 2 WHERE zug_id = ? AND platz_id = ?";
		PreparedStatement ps = null;
		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, zug_id);
			ps.setInt(2, platz_id);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public List<String> getHaltestellen() {
		// alle Züge holen, die einen Eintrag mit freiem Status haben

		Connection conn = null;

		String sql = "SELECT DISTINCT haltestelle FROM haltestelle";
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			List<String> haltestellen = new ArrayList<String>();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				haltestellen.add(rs.getString("Haltestelle"));
			}
			rs.close();
			ps.close();
			return haltestellen;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

}
