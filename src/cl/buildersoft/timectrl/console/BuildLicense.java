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
		String domainKey = args[0];

		GenerateLicense gl = new GenerateLicense();
		BSConnectionFactory cf = new BSConnectionFactory();
		Connection conn = cf.getConnection(domainKey);

		String days = args.length == 2 ? args[1] : gl.getMaxDays().toString();
		String result = gl.generateLicense(conn, days, domainKey);
		cf.closeConnection(conn);

		ArrayList<String> out = new ArrayList<String>();
		out.add(result);

		return out;
	}
}
