package cl.buildersoft.timectrl.security;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Calendar;
import java.util.List;

import cl.buildersoft.framework.database.BSBeanUtils;
import cl.buildersoft.framework.database.BSmySQL;
import cl.buildersoft.framework.exception.BSSystemException;
import cl.buildersoft.framework.exception.BSUserException;
import cl.buildersoft.framework.util.BSDateTimeUtil;
import cl.buildersoft.framework.util.BSSecurity;
import cl.buildersoft.timectrl.business.beans.Machine;

public class GenerateLicense {
	private Integer maxDays = 90;
	private static final String WEB_INF = "WEB-INF";

	public String generateLicense(String server, String database, String user, String password, String webFolder, String days) {
		BSBeanUtils bu = new BSBeanUtils();
		Connection conn = bu.getConnection("com.mysql.jdbc.Driver", server, database, password, user);
		return generateLicense(conn, webFolder, days);
	}

	public String generateLicense(Connection conn, String webFolder, String days) {
		validateWebFolder(webFolder);
		validateDays(days);

		BSBeanUtils bu = new BSBeanUtils();
//		Connection conn = bu.getConnection("com.mysql.jdbc.Driver", server, database, password, user);

		String serials = getSerialNumbers(bu, conn);

		serials += "#";
		serials += todayPlus30Days();

		BSSecurity security = new BSSecurity();
		String licenseCrypt = security.encript3des(serials);

		String licenseFile =    plusWebInf(webFolder) + File.separator + "LicenseFile.dat";

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(licenseFile, "UTF-8");
			writer.println(licenseCrypt);
		} catch (Exception e) {
			throw new BSSystemException(e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		new BSmySQL().closeConnection(conn);

		return "Done!";
	}

	private void validateDays(String days) {
		Integer daysInteger = Integer.parseInt(days);
		if (daysInteger < getMaxDays()) {
			setMaxDays(daysInteger);
		}
	}

	private void validateWebFolder(String webFolder) {
		File folder = new File(webFolder);
		String message = null;
		if (!folder.exists()) {
			message = "Folder '" + webFolder + "' not exists";
		} else {
			if (!folder.isDirectory()) {
				message = "'" + webFolder + "' is not a folder";
			} else {
				if (!webFolder.endsWith(WEB_INF)) {
					String webInf = plusWebInf(webFolder);
					validateWebFolder(webInf);
				}
			}
		}

		if (message != null) {
			throw new BSUserException("", message);
		}

	}

	private String plusWebInf(String webFolder) {
		String webInf = webFolder + File.separator + WEB_INF;
		return webInf;
	}

	private String todayPlus30Days() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, getMaxDays());

		String out = BSDateTimeUtil.date2String(BSDateTimeUtil.calendar2Date(calendar), "yyyy-MM-dd");

		return out;
	}

	private String getSerialNumbers(BSBeanUtils bu, Connection conn) {
		@SuppressWarnings("unchecked")
		List<Machine> machines = (List<Machine>) bu.listAll(conn, new Machine());
		String out = "";
		for (Machine machine : machines) {
			out += machine.getSerial() + ",";
		}
		if (out.length() > 0) {
			out = out.substring(0, out.length() - 1);
		}
		return out;
	}

	public Integer getMaxDays() {
		return this.maxDays;
	}

	public void setMaxDays(Integer maxDays) {
		this.maxDays = maxDays;
	}

}
