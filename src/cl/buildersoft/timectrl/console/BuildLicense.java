package cl.buildersoft.timectrl.console;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.buildersoft.framework.util.BSConnectionFactory;
import cl.buildersoft.timectrl.business.process.AbstractProcess;
import cl.buildersoft.timectrl.business.process.ExecuteProcess;
import cl.buildersoft.timectrl.security.GenerateLicense;

public class BuildLicense extends AbstractProcess implements ExecuteProcess {
	private static final Logger LOG = Logger.getLogger(BuildLicense.class.getName());

	private String[] validArguments = { "DOMAIN" };

	public static void main(String[] args) {
		BuildLicense bl = new BuildLicense();
		List<String> response = bl.doExecute(args);
		for (String resp : response) {
			LOG.log(Level.INFO, resp);
		}
	}

	@Override
	protected String[] getArguments() {
		return this.validArguments;
	}

	@Override
	public List<String> doExecute(String[] args) {
		validateArguments(args, true);

		GenerateLicense gl = new GenerateLicense();
		BSConnectionFactory cf = new BSConnectionFactory();
		Connection conn = cf.getConnection(args[0]);

		String days = args.length == 2 ? args[1] : gl.getMaxDays().toString();
		String result = gl.generateLicense(conn, days);
		cf.closeConnection(conn);

		ArrayList<String> out = new ArrayList<String>();
		out.add(result);

		/**
		 * if (args.length < 5 || args.length > 6) { showHelp(gl); } else {
		 * String server = args[0]; String database = args[1]; String user =
		 * args[2]; String password = args[3]; String webRoot = args[4]; }
		 */
		return out;
	}

	/**
	 * <code>
	private static void showHelp(GenerateLicense gl) {
		String example = "$> BuildLicense localhost timectrl root admin D:\\workspace\\timectrl-web\\WebContent "
				+ gl.getMaxDays() + " \n";
		example += "$> BuildLicense localhost timectrl root admin \"D:\\Apache Tomcat\\webapp\\timectrl-web\\WebContent\"";
		System.out.println("\nComando:");
		System.out.println("$> BuildLicense <Server> <DataDaseName> <User> <Password> <WebFolder>\n");
		System.out.println("Server: servidor donde esta la base de datos con los datos de las máquinas");
		System.out.println("DataDaseName: Nombre de la base de datos 'timectrl'");
		System.out.println("Usuario: Usuario de la base de datos");
		System.out.println("Password: Password de la base de datos");
		System.out
				.println("Web Folder: Carpeta donde está la aplicación web de relojes. Si esta carpeta tiene espacios en blanco, envolver entre comillas (\"\")");
		System.out.println("Días para expirar (OPCIONAL): Cantidad de días en cuanto expirarpa la licencia (máximo "
				+ gl.getMaxDays() + ")");

		System.out.println();
		System.out.println("Ejemplos:");
		System.out.println(example);
		System.out.println();

	}
</code>
	 */
}
