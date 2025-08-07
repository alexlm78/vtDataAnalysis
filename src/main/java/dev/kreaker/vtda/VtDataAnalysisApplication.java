package dev.kreaker.vtda;

import dev.kreaker.vtda.exception.ConfigurationException;
import dev.kreaker.vtda.exception.ErrorCode;
import dev.kreaker.vtda.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@Slf4j
public class VtDataAnalysisApplication implements CommandLineRunner {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ConfigurationService configurationService;

	public static void main(String[] args) {
		SpringApplication.run(VtDataAnalysisApplication.class, args);
	}

	@Override
	public void run(String... args) {
		try {
			// Check if help was requested
			if (configurationService.isHelpRequested()) {
				System.out.println(configurationService.getHelpText());
				System.exit(ErrorCode.SUCCESS.getExitCode());
				return;
			}
			
			// Validate configuration
			try {
				configurationService.validateConfiguration();
				if (configurationService.isVerboseMode()) {
					log.info("Configuration validation completed successfully");
				}
			} catch (ConfigurationException e) {
				System.err.println("Configuration Error: " + e.getMessage());
				System.err.println("\nUse --help or -h for usage information.");
				System.exit(ErrorCode.CONFIG_ERROR.getExitCode());
				return;
			}
			
			// For now, use the old implementation until other services are implemented
			// This will be replaced when metadata extraction service is implemented
			String schema = configurationService.getDatabaseConfig().getSchema();
			if (configurationService.isVerboseMode()) {
				log.info("Extracting metadata for schema: {}", schema);
			}
			
			getTablesSchema(schema);
			
		} catch (ConfigurationException e) {
			if (!configurationService.isQuietMode()) {
				System.err.println("Configuration Error: " + e.getMessage());
				System.err.println("\nUse --help or -h for usage information.");
			}
			System.exit(ErrorCode.CONFIG_ERROR.getExitCode());
		} catch (Exception e) {
			if (!configurationService.isQuietMode()) {
				System.err.println("Unexpected error: " + e.getMessage());
				if (configurationService.isVerboseMode()) {
					e.printStackTrace();
				}
			}
			System.exit(ErrorCode.UNEXPECTED_ERROR.getExitCode());
		}
	}

	public void getTablesSchema( String schema ) throws Exception {
		//String esquema = "REPORTUSER"; // Cambia por tu esquema en mayúsculas
		String archivoSalida = "tablas_columnas_"+schema+".csv";

		String sql = """
            SELECT t.table_name, c.column_name, c.data_type, c.data_length, c.nullable, c.data_default
            FROM all_tables t
            JOIN all_tab_columns c ON t.table_name = c.table_name AND t.owner = c.owner
            WHERE t.owner = ?
            ORDER BY t.table_name, c.column_id
            """;

		List<Map<String, Object>> filas = jdbcTemplate.queryForList(sql, schema);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida))) {
			writer.write("table_name,column_name,data_type,data_length,nullable,data_default");
			writer.newLine();

			for (Map<String, Object> fila : filas) {
				String tableName = String.valueOf(fila.get("table_name"));
				String columnName = String.valueOf(fila.get("column_name"));
				String dataType = String.valueOf(fila.get("data_type"));
				String dataLength = String.valueOf(fila.get("data_length"));
				String nullable = String.valueOf(fila.get("nullable"));
				String dataDefault = fila.get("data_default") == null ? "" :
						fila.get("data_default").toString().replace("\n", " ").replace("\r", " ").replace(",", " ");

				writer.write(String.format("%s,%s,%s,%s,%s,%s",
						tableName, columnName, dataType, dataLength, nullable, dataDefault));
				writer.newLine();
			}
			System.out.println("Exportación completada: " + archivoSalida);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getDDLFromTable( String schema, String table ) {
		//String schema = "NOMBRE_DEL_SCHEMA"; // Cambia por tu esquema en mayúsculas
		//String table = "NOMBRE_DE_LA_TABLA"; // Cambia por tu tabla en mayúsculas
		String outputFile = "ddl_" + schema + "_" + table + ".sql";

		// 1. Obtener columnas
		String sqlColumns = """
            SELECT column_name, data_type, data_length, data_precision, data_scale, nullable, data_default, column_id
            FROM all_tab_columns
            WHERE owner = ? AND table_name = ?
            ORDER BY column_id
            """;

		List<Map<String, Object>> columns = jdbcTemplate.queryForList(sqlColumns, schema, table);

		if (columns.isEmpty()) {
			System.out.println("No se encontraron columnas para la tabla " + schema + "." + table);
			return;
		}

		// 2. Obtener columnas de la clave primaria
		String sqlPK = """
            SELECT acc.column_name
            FROM all_constraints ac
            JOIN all_cons_columns acc ON ac.owner = acc.owner AND ac.constraint_name = acc.constraint_name
            WHERE ac.owner = ? AND ac.table_name = ? AND ac.constraint_type = 'P'
            ORDER BY acc.position
            """;
		List<String> pkColumns = jdbcTemplate.queryForList(sqlPK, String.class, schema, table);

		// 3. Generar DDL
		StringBuilder ddl = new StringBuilder();
		ddl.append("CREATE TABLE ").append(schema).append(".").append(table).append(" (\n");

		for (int i = 0; i < columns.size(); i++) {
			Map<String, Object> col = columns.get(i);
			String colName = (String) col.get("column_name");
			String dataType = (String) col.get("data_type");
			Number dataLength = (Number) col.get("data_length");
			Number dataPrecision = (Number) col.get("data_precision");
			Number dataScale = (Number) col.get("data_scale");
			String nullable = (String) col.get("nullable");
			Object dataDefault = col.get("data_default");

			ddl.append("  ").append(colName).append(" ").append(dataType);

			// Tipos con longitud
			if (Arrays.asList("CHAR", "NCHAR", "VARCHAR2", "NVARCHAR2").contains(dataType)) {
				ddl.append("(").append(dataLength).append(")");
			} else if ("NUMBER".equals(dataType) && dataPrecision != null) {
				ddl.append("(").append(dataPrecision);
				if (dataScale != null && dataScale.intValue() > 0) {
					ddl.append(",").append(dataScale);
				}
				ddl.append(")");
			}

			// Valor por defecto
			if (dataDefault != null) {
				String def = dataDefault.toString().replace("\n", " ").replace("\r", " ").trim();
				ddl.append(" DEFAULT ").append(def);
			}

			// Nulos
			if ("N".equals(nullable)) {
				ddl.append(" NOT NULL");
			}

			// Coma si no es la última columna o si hay PK
			if (i < columns.size() - 1 || !pkColumns.isEmpty()) {
				ddl.append(",");
			}
			ddl.append("\n");
		}

		// Clave primaria
		if (!pkColumns.isEmpty()) {
			ddl.append("  CONSTRAINT pk_").append(table.toLowerCase())
					.append(" PRIMARY KEY (")
					.append(String.join(", ", pkColumns))
					.append(")\n");
		}

		ddl.append(");");

		// 4. Guardar en archivo
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
			writer.write(ddl.toString());
			System.out.println("DDL exportado a: " + outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
