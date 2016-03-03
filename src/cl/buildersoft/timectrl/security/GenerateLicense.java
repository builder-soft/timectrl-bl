package cl.buildersoft.timectrl.security;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Calendar;
import java.util.List;

import cl.buildersoft.framework.database.BSBeanUtils;
import cl.buildersoft.framework.exception.BSSystemException;
import cl.buildersoft.framework.util.BSConfig;
import cl.buildersoft.framework.util.BSDateTimeUtil;
import cl.buildersoft.framework.util.BSSecurity;
import cl.buildersoft.timectrl.business.beans.Machine;

public class GenerateLicense {
	private Integer maxDays = 90;

	/**
	 * <code>
	public String generateLicense(String dsName, String days) {
		BSConnectionFactory cf = new BSConnectionFactory();
		Connection conn = cf.getConnection(dsName);
		String out = generateLicense(conn, days,null);
		cf.closeConnection(conn);

		return out;
	}
</code>
	 */
	public String generateLicense(Connection conn, String days, String domainKey) {
		validateDays(days);

		BSBeanUtils bu = new BSBeanUtils();

		String serials = getSerialNumbers(bu, conn);

		serials += "#";
		serials += todayPlus30Days();

		BSSecurity security = new BSSecurity();
		String licenseCrypt = security.encript3des(serials);

		BSConfig config = new BSConfig();

		String licenseFile = config.fixPath(System.getenv("BS_PATH")) + "license." + domainKey + ".dat";

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

		return "Done!";
	}

	private void validateDays(String days) {
		Integer daysInteger = Integer.parseInt(days);
		if (daysInteger < getMaxDays()) {
			setMaxDays(daysInteger);
		}
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
