package ke.co.skyworld.Model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.crypto.Cipher;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ConfigReader {
    private static final byte[] secretKey = "aiajd9292UA7HK38381JSHA393JAKASt".getBytes(StandardCharsets.UTF_8);
    private static String dbType;
    private String dbName;
    private String dbHost;
    private int dbPort;
    private String username;
    private String password;
    private static String serverHost;
    private static int serverPort;

    public void setDbType(String dbType) { this.dbType = dbType; }
    public void setDbName(String dbName) { this.dbName = dbName; }
    public void setDbHost(String dbHost) { this.dbHost = dbHost; }
    public void setDbPort(int dbPort) { this.dbPort = dbPort; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setServerHost(String serverHost) { this.serverHost = serverHost; }
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }

    public static String getDbType() { return dbType; }
    public String getDbName() { return dbName; }
    public String getDbHost() { return dbHost; }
    public int getDbPort() { return dbPort; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public static String getServerHost() { return serverHost; }
    public static int getServerPort() { return serverPort; }

    public ConfigReader(File configFile) {
        parseConfigFile(configFile);
    }

    public void parseConfigFile(File configFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(configFile);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            this.setDbType(xpath.evaluate("/configuration/database/type", document));
            this.setDbName(xpath.evaluate("/configuration/database/name", document));
            this.setDbHost(xpath.evaluate("/configuration/database/host", document));
            this.setDbPort(Integer.parseInt(xpath.evaluate("/configuration/database/port", document)));
            this.setServerHost(xpath.evaluate("/configuration/api/host", document));
            this.setServerPort(Integer.parseInt(xpath.evaluate("/configuration/api/port", document)));

            // Process username
            Node usernameNode = (Node) xpath.evaluate("/configuration/database/username", document, XPathConstants.NODE);
            if (usernameNode != null) {
                String username = usernameNode.getTextContent();
                String encryptedAttrUsername = ((Element) usernameNode).getAttribute("ENCRYPTED");
                if ("no".equalsIgnoreCase(encryptedAttrUsername)) {
                    username = encrypt(username);
                    ((Element) usernameNode).setTextContent(username);
                    ((Element) usernameNode).setAttribute("ENCRYPTED", "yes");
                }
                this.setUsername(username);
            }

            // Process password
            Node passwordNode = (Node) xpath.evaluate("/configuration/database/password", document, XPathConstants.NODE);
            if (passwordNode != null) {
                String password = passwordNode.getTextContent();
                String encryptedAttrPassword = ((Element) passwordNode).getAttribute("ENCRYPTED");
                if ("no".equalsIgnoreCase(encryptedAttrPassword)) {
                    password = encrypt(password);
                    ((Element) passwordNode).setTextContent(password);
                    ((Element) passwordNode).setAttribute("ENCRYPTED", "yes");
                }
                this.setPassword(password);
            }

            // Save changes back to the XML file
            saveDocumentToFile(document, configFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String decrypt(String encryptedData, byte[] secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveDocumentToFile(Document doc, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    public Connection getConnection() throws SQLException {
        String connectionUrl = buildConnectionUrl();
        String decryptedUsername = decrypt(this.getUsername(), secretKey);
        String decryptedPassword = decrypt(this.getPassword(), secretKey);

        return DriverManager.getConnection(connectionUrl, decryptedUsername, decryptedPassword);
    }

    private String buildConnectionUrl() {
        switch (this.getDbType().toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s", this.getDbHost(), this.getDbPort(), this.getDbName());
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", this.getDbHost(), this.getDbPort(), this.getDbName());
            case "mssql":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", this.getDbHost(), this.getDbPort(), this.getDbName());
            default:
                throw new IllegalArgumentException("Unsupported database type: " + this.getDbType());
        }
    }

    public void createTables(Connection connection) throws SQLException {
        String[] createTableCommands = getTableCreationCommands(this.getDbType());

        try (Statement statement = connection.createStatement()) {
            for (String sql : createTableCommands) {
                try {
                    statement.execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private String[] getTableCreationCommands(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return getMySQLTableCommands();
            case "postgresql":
                return getPostgreSQLTableCommands();
            case "mssql":
                return getMSSQLTableCommands();
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    // actual SQL commands for each database type
    private String[] getMySQLTableCommands() {
        return new String[] {
                "CREATE TABLE IF NOT EXISTS company (" +
                        "company_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(200) NOT NULL, " +
                        "reg_no VARCHAR(50), " +
                        "industry VARCHAR(50), " +
                        "mission VARCHAR(200), " +
                        "phone VARCHAR(10), " +
                        "email VARCHAR(50) NOT NULL, " +
                        "website VARCHAR(50), " +
                        "branch VARCHAR(50) NOT NULL, " +
                        "postal_address VARCHAR(200) NOT NULL, " +
                        "company_inception DATE NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME, " +
                        "UNIQUE INDEX unique_name (name), " +
                        "UNIQUE INDEX unique_phone (phone), " +
                        "UNIQUE INDEX unique_email (email), " +
                        "UNIQUE INDEX unique_website (website), " +
                        "UNIQUE INDEX unique_reg_no (reg_no))",


                "CREATE TABLE IF NOT EXISTS department (" +
                        "department_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "company_id BIGINT NOT NULL, " +
                        "name VARCHAR(200) NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME, " +
                        "UNIQUE INDEX unique_name (name), " +
                        "FOREIGN KEY (company_id) REFERENCES company(company_id))",


                "CREATE TABLE IF NOT EXISTS earning_types (" +
                        "earning_types_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "company_id BIGINT NOT NULL, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME, " +
                        "UNIQUE INDEX unique_name (name), " +
                        "FOREIGN KEY (company_id) REFERENCES company(company_id))",


                "CREATE TABLE IF NOT EXISTS deduction_types (" +
                        "deduction_types_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "company_id BIGINT NOT NULL, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME, " +
                        "UNIQUE INDEX unique_name (name), " +
                        "FOREIGN KEY (company_id) REFERENCES company(company_id))",


                "CREATE TABLE IF NOT EXISTS employee (" +
                        "employee_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "company_id BIGINT NOT NULL, " +
                        "employee_name VARCHAR(50) NOT NULL, " +
                        "employee_number VARCHAR(10) NOT NULL, " +
                        "id_number VARCHAR(8) NOT NULL, " +
                        "nssf_no VARCHAR(50), " +
                        "nhif_no VARCHAR(50), " +
                        "kra_pin VARCHAR(50), " +
                        "phone VARCHAR(10), " +
                        "email VARCHAR(50), " +
                        "date_of_birth DATE, " +
                        "employment_start_date VARCHAR(10) NOT NULL, " +
                        "employment_termination_date VARCHAR(10), " +
                        "employment_status VARCHAR(20) NOT NULL, " +
                        "employee_position VARCHAR(50) NOT NULL, " +
                        "department_id BIGINT, " +
                        "gender VARCHAR(10), " +
                        "username VARCHAR(250), " +
                        "password VARCHAR(255), " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME, " +
                        "UNIQUE INDEX unique_employee_number (employee_number), " +
                        "UNIQUE INDEX unique_id_number (id_number), " +
                        "UNIQUE INDEX unique_nssf_no (nssf_no), " +
                        "UNIQUE INDEX unique_nhif_no (nhif_no), " +
                        "UNIQUE INDEX unique_phone (phone), " +
                        "UNIQUE INDEX unique_kra_pin (kra_pin), " +
                        "INDEX idx_gender (gender), " +
                        "INDEX idx_employee_name (employee_name), " +
                        "UNIQUE INDEX unique_email (email), " +
                        "UNIQUE INDEX unique_username (username), " +
                        "FOREIGN KEY (company_id) REFERENCES company(company_id), " +
                        "FOREIGN KEY (department_id) REFERENCES department(department_id))",


                "CREATE TABLE IF NOT EXISTS period (" +
                        "period_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "period VARCHAR(10) NOT NULL, " +    // yyyy-MM
                        "status VARCHAR(10) NOT NULL, " +
                        "UNIQUE INDEX unique_period (period), "+
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME)",


                "CREATE TABLE IF NOT EXISTS employee_earnings (" +
                        "employee_earnings_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "earning_types_id BIGINT NOT NULL, " +
                        "employee_id BIGINT NOT NULL, " +
                        "amount FLOAT NOT NULL, " +
                        "period_id BIGINT NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (earning_types_id) REFERENCES earning_types(earning_types_id), " +
                        "FOREIGN KEY (employee_id) REFERENCES employee(employee_id), " +
                        "FOREIGN KEY (period_id) REFERENCES period(period_id))",


                "CREATE TABLE IF NOT EXISTS employee_deductions (" +
                        "employee_deductions_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "deduction_types_id BIGINT NOT NULL, " +
                        "employee_id BIGINT NOT NULL, " +
                        "period_id BIGINT NOT NULL, " +
                        "amount FLOAT NOT NULL, " +
                        "date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "date_modified DATETIME, " +
                        "FOREIGN KEY (deduction_types_id) REFERENCES deduction_types(deduction_types_id), " +
                        "FOREIGN KEY (employee_id) REFERENCES employee(employee_id), " +
                        "FOREIGN KEY (period_id) REFERENCES period(period_id))"

        };
    }

    private String[] getPostgreSQLTableCommands() {
        return new String[] {

        };
    }



    private String[] getMSSQLTableCommands() {
        return new String[]{

        };
    }
}

