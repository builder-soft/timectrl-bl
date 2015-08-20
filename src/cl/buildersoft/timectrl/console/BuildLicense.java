package cl.buildersoft.timectrl.console;

import java.sql.Connection;

import cl.buildersoft.timectrl.business.console.AbstractConsoleService;
import cl.buildersoft.timectrl.security.GenerateLicense;

public class BuildLicense extends AbstractConsoleService {
	public static void main(String[] args) {
		BuildLicense bl = new BuildLicense();
		bl.doWork(args);
	}
	
	private void doWork(String[] args) {
		GenerateLicense gl = new GenerateLicense();
		Connection conn = getConnection();

		String days = args.length == 1 ? args[0] : ("" + gl.getMaxDays());
		String result = gl.generateLicense(conn, getWebPath(), days);

		System.out.println(result);

		/**
		if (args.length < 5 || args.length > 6) {
			showHelp(gl);
		} else {
			String server = args[0];
			String database = args[1];
			String user = args[2];
			String password = args[3];
			String webRoot = args[4];
		}
		*/
	}

	/**<code>
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
</code>*/
}
