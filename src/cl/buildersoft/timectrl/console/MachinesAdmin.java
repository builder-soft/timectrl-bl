package cl.buildersoft.timectrl.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Logger;

import cl.buildersoft.framework.database.BSBeanUtils;
import cl.buildersoft.framework.exception.BSSystemException;
import cl.buildersoft.framework.util.BSConnectionFactory;
import cl.buildersoft.timectrl.api._zkemProxy;
import cl.buildersoft.timectrl.business.beans.Machine;
import cl.buildersoft.timectrl.business.process.AbstractProcess;
import cl.buildersoft.timectrl.business.process.ExecuteProcess;
import cl.buildersoft.timectrl.business.services.MachineService2;
import cl.buildersoft.timectrl.business.services.impl.MachineServiceImpl2;
import cl.buildersoft.timectrl.security.GenerateLicense;

public class MachinesAdmin extends AbstractProcess implements ExecuteProcess {
	private static final Logger LOG = Logger.getLogger(MachinesAdmin.class.getName());

	private String[] validArguments = { "DOMAIN" };

	public static void main(String[] args) {
		try {
			MachinesAdmin machinesAdmin = new MachinesAdmin();
			// machinesAdmin.init();
			machinesAdmin.doExecute(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String[] getArguments() {
		return this.validArguments;
	}

	@Override
	public List<String> doExecute(String[] args) {
		validateArguments(args, false);
		Boolean keep = true;
		Integer option = 0;

		String domainKey = args[0];
		String days = null;//args.length == 2 ? args[1] : gl.getMaxDays().toString();

		BSBeanUtils bu = new BSBeanUtils();
		BSConnectionFactory cf = new BSConnectionFactory();
		Connection conn = cf.getConnection(domainKey);

		try {
			while (keep) {
				showMenu();
				option = readOption();
				switch (option) {
				case 1: // Listar
					listMachines(conn, bu);
					break;
				case 2: // Ingresar
					Machine machine = readMachineByConsole(conn);
					if (machine != null) {
						bu.insert(conn, machine);
					}
					readString("Listo, presione ENTER y continue.");
					// showMenu();
					break;
				case 3: // Actualizar serie
					refreshSerial(conn);

					break;
				case 4: // Borrar
					deleteMachine(conn, bu);

					break;
				case 5: // Salir
					GenerateLicense gl = new GenerateLicense();
					
					days = args.length == 2 ? args[1] : gl.getMaxDays().toString();
					
					gl.generateLicense(conn, days, domainKey);
					keep = false;
					break;
				default:
					readString("Opcion incorrecta, presione ENTER.");
					break;
				}
			}
		} finally {
			cf.closeConnection(conn);
		}
		return null;
	}

	private void refreshSerial(Connection conn) {
		if (confirm("¿Desea refrescar la serie de todas las maquinas?")) {
			BSBeanUtils bu = new BSBeanUtils();
			@SuppressWarnings("unchecked")
			List<Machine> machines = (List<Machine>) bu.listAll(conn, new Machine());
			for (Machine machine : machines) {
				refreshOneSerial(conn, machine.getId());
			}
		} else {
			refreshOneSerial(conn, null);
		}
	}

	private void refreshOneSerial(Connection conn, Long id) {
		id = id == null ? readLong("Ingrese Id de maquina: ") : id;

		Machine machine = new Machine();
		machine.setId(id);
		BSBeanUtils bu = new BSBeanUtils();

		if (bu.search(conn, machine)) {
			MachineService2 service = new MachineServiceImpl2();

			// machine.setIp(machine.getIp());
			// machine.setPort(machine.getPort());

			System.out.println("Buscando maquina '" + machine.getName() + "'...");

			_zkemProxy api = service.connect(conn, machine);
			if (api == null) {
				System.out.println("La maquina no fue encontrada.");
			} else {
				machine.setSerial(service.readSerial(api));
			}
			bu.update(conn, machine);
		} else {
			readString("Maquina con ID '" + id + "' no encontrada");
		}
	}

	private void deleteMachine(Connection conn, BSBeanUtils bu) {
		Long id = readLong("Ingrese Id de maquina: ");

		Machine machine = new Machine();
		machine.setId(id);

		if (bu.search(conn, machine)) {
			if (confirm("Esta seguro de eliminar la maquina '" + machine.getName() + "'")) {
				bu.delete(conn, machine);
			}

		} else {
			readString("Maquina con ID '" + id + "' no encontrada");
		}

	}

	private Boolean confirm(String msg) {
		return readString(msg + ", escriba 'Yes' para confirmar:").equalsIgnoreCase("Yes");
	}

	private Machine readMachineByConsole(Connection conn) {
		Machine machine = null;
		MachineService2 service = new MachineServiceImpl2();
		Boolean done = false;

		String ip = "";
		while (!done) {
			ip = readString("Ingrese IP (ENTER cancela):");
			if (ip.length() > 0) {
				machine = new Machine();
				machine.setIp(ip);
				machine.setPort(readInteger("Ingrese Puerto:"));
				System.out.println("Buscando maquina...");

				_zkemProxy api = service.connect(conn, machine);
				if (api == null) {
					System.out.println("La maquina no fue encontrada, reintente o deje en blanco para volver.");
				} else {
					machine.setSerial(service.readSerial(api));
					machine.setName(readString("Ingrese nombre:"));
					machine.setGroup(service.getDefaultGroup(conn));

					done = true;
					service.disconnect(api);
				}
			} else {
				machine = null;
				done = true;
			}
		}
		return machine;
	}

	private void listMachines(Connection conn, BSBeanUtils bu) {
		@SuppressWarnings("unchecked")
		List<Machine> machines = (List<Machine>) bu.listAll(conn, new Machine());

		Integer len = 121;
		String message = "+" + repeat("-", len) + "+\n";
		message += "|" + rPad("Id", 5) + "|";
		message += rPad("Nombre", 50) + "|";
		message += rPad("IP", 15) + "|";
		message += rPad("Port", 5) + "|";
		message += rPad("Ultimo Acceso", 21) + "|";
		message += rPad("Serie", 20) + "|\n";
		message += "+" + repeat("-", len) + "+\n";

		for (Machine machine : machines) {
			message += "|" + rPad("" + machine.getId(), 5) + "|";
			message += rPad(machine.getName(), 50) + "|";
			message += rPad(machine.getIp(), 15) + "|";
			message += rPad("" + machine.getPort(), 5) + "|";
			message += rPad("" + machine.getLastAccess(), 21) + "|";
			message += rPad(machine.getSerial(), 20) + "|";

			message += "\n";
		}
		message += "+" + repeat("-", len) + "+\n";

		System.out.println(message);
	}

	private String repeat(String s, int n) {
		return new String(new char[n]).replace("\0", s);
	}

	private Integer readOption() {
		Integer out = 0;
		while (out < 1 || out > 5) {
			out = readInteger("Ingrese Opcion:");
		}
		return out;
	}

	private Long readLong(String msg) {
		System.out.print(msg);
		Boolean sucess = false;
		Long out = 0L;
		String value = "";
		while (!sucess) {
			value = readString();
			try {
				out = Long.parseLong(value);
				sucess = true;
			} catch (Exception e) {
				sucess = false;
			}
		}
		return out;
	}

	private Integer readInteger(String msg) {
		System.out.print(msg);
		return readInteger();
	}

	private Integer readInteger() {
		Boolean sucess = false;
		Integer out = 0;
		String integer = "";
		while (!sucess) {
			integer = readString();
			try {
				out = Integer.parseInt(integer);
				sucess = true;
			} catch (Exception e) {
				sucess = false;
			}
		}
		return out;
	}

	private String readString(String msg) {
		System.out.print(msg);
		return readString();

	}

	private String readString() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String out = null;
		try {
			out = br.readLine();
		} catch (IOException e) {
			throw new BSSystemException(e);
		}
		return out;
	}

	private void showMenu() {
		String message = "Menu\n";
		message += "----\n";
		message += "1.- Listar maquinas\n";
		message += "2.- Agregar maquina\n";
		message += "3.- Actualizar serie\n";
		message += "4.- Borrar maquina\n";
		message += "5.- Salir";
		System.out.println(message);

	}

	public String rPad(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	public String lPad(String s, int n) {
		return String.format("%1$" + n + "s", s);
	}

	/**
	 * <code>
	private static void showHelp() {
		String example = "$> MachinesAdmin localhost timectrl root admin";

		System.out.println("\nComando:");
		System.out.println("$> MachinesAdmin <Server> <DataDaseName> <User> <Password>\n");
		System.out.println("Server: servidor donde esta la base de datos con los datos de las mï¿½quinas");
		System.out.println("DataDaseName: Nombre de la base de datos 'timectrl'");
		System.out.println("Usuario: Usuario de la base de datos");
		System.out.println("Password: Password de la base de datos");

		System.out.println();
		System.out.println("Ejemplo:");
		System.out.println(example);
		System.out.println();
	}
	</code>
	 */
}
