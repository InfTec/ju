package ch.inftec.ju.db;

import java.awt.Image;

import javax.swing.Icon;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Class containing connection information to connect to an
 * Oracle database.
 * 
 * @author tgdmemae
 *
 */
public class ConnectionInfoImpl implements ConnectionInfo {
	private String name;
	private String connectionString;
	private String userName;
	private String password;
	private String schema;
	private Icon icon;
	private Image image;
	
	public ConnectionInfoImpl() {
	}
	
	/**
	 * Creates a new connection info object.
	 * @param name Name of the connection
	 * @param connectionString Oracle specific connection string
	 * @param userName User name
	 * @param password Password
	 * @param schema The DB schema to be used. This is not completely integrated in to MyTTS, it is rather a
	 * workaround to be able to use a different DB user than the actual target Schema.
	 * @param passwordProtected If true, all write operations will require the user to confirm the DB password
	 * @param icon Icon representing the database connection
	 * @param image Image representing the database connection
	 */
	public ConnectionInfoImpl(String name, String connectionString, String userName, String password, String schema, boolean passwordProtected, Icon icon, Image image) {
		this.name = name;
		this.connectionString = connectionString;
		this.userName = userName;
		this.password = password;
		this.schema = schema;
		this.icon = icon;
		this.image = image;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	@Override
	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return this.getName();
	}
	
	@Override
	public int compareTo(ConnectionInfo o) {
		return ObjectUtils.compare(this.getName(), o == null ? null : o.getName());
	}
}
