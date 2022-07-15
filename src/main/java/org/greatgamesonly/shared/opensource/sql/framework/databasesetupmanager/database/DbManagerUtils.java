package org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.database;

import org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.exceptions.DbManagerException;
import org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.exceptions.errors.DbManagerError;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.nio.file.Files.readString;

public class DbManagerUtils {
    private static final Properties properties = new Properties();

    protected static String getConfigurationProperty(String keyName) {
        String result = getProperties().getProperty(keyName);
        if(result == null || result.isBlank()) {
            result = System.getenv(keyName);
        }
        return result;
    }

    protected static String getDatabaseUrl() {
        String result = getConfigurationProperty("datasource.url");
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("quarkus.datasource.url");
        }
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("DATABASE_URL");
        }
        return result;
    }

    protected static String getDatabaseUsername() {
        String result = getConfigurationProperty("datasource.username");
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("quarkus.datasource.username");
        }
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("DATABASE_USERNAME");
        }
        return result;
    }

    protected static String getDatabasePassword() {
        String result = getConfigurationProperty("datasource.password");
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("quarkus.datasource.password");
        }
        if(result == null || result.isBlank()) {
            result = getConfigurationProperty("DATABASE_PASSWORD");
        }
        return result;
    }

    protected static String getSeedFileResourceDirectory() {
        String result = getConfigurationProperty("databasesetupmanager_db_seed_files_directory");
        if(result == null || result.isBlank()) {
            result = "seeds";
        }
        return result;
    }

    protected static String getMigrationFileResourceDirectory() {
        String result = getConfigurationProperty("databasesetupmanager_db_migration_files_directory");
        if(result == null || result.isBlank()) {
            result = "migrations";
        }
        return result;
    }

    protected static Properties getProperties() {
        if(properties.isEmpty()) {
            try {
                properties.load(DbManagerUtils.class.getClassLoader().getResourceAsStream("config.properties"));
            } catch (IOException ignore) {}
        }
        if(properties.isEmpty()) {
            try {
                properties.load(DbManagerUtils.class.getClassLoader().getResourceAsStream("application.properties"));
            } catch (IOException ignore) {}
        }
        return properties;
    }

    public static void runDbManager() throws DbManagerException {
        DbManagerStatusDataRepository dbManagerStatusDataRepository = new DbManagerStatusDataRepository();
        DbManagerStatusData dbManagerStatusData = null;

        dbManagerStatusDataRepository.executeQueryRaw("CREATE TABLE IF NOT EXISTS \"databasesetupmanager_setup_status_info\" ("+
                "id serial PRIMARY_KEY,"+
                "seed_files_ran BOOLEAN NOT NULL DEFAULT FALSE,"+
                "filename_last_migration_file_successfully_ran VARCHAR(10000) NOT NULL);");

        if(dbManagerStatusDataRepository.countByColumn("id", 1) <= 0) {
            dbManagerStatusData = dbManagerStatusDataRepository.insertOrUpdate(new DbManagerStatusData(1L,false, ""));
        } else {
            dbManagerStatusData = dbManagerStatusDataRepository.getAll().get(0);
        }

        if(!dbManagerStatusData.getSeedFilesRan()) {
            List<String> seedFileNames = new ArrayList<>();
            try {
                seedFileNames = getResourceFiles(getSeedFileResourceDirectory());
                    /*Files.walk(Paths.get("/path/to/folder"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());*/
            } catch (IOException e) {
                throw new DbManagerException(DbManagerError.UNABLE_TO_FETCH_SEED_FILES, e.getMessage());
            }

            try {
                HashMap<Long, String> seedNumbersOnlyAndFilenames = new HashMap<>();
                seedFileNames.forEach(seedFileName -> seedNumbersOnlyAndFilenames.put(Long.parseLong(seedFileName.replaceAll("[^0-9]", "")), seedFileName));
                List<Long> seedFilenameNumbersOnly = seedNumbersOnlyAndFilenames.keySet().stream().sorted().collect(Collectors.toList());
                for (Long seedFilenameNumberOnly : seedFilenameNumbersOnly) {
                    String sql = readString(getFileFromResource(seedNumbersOnlyAndFilenames.get(seedFilenameNumberOnly)).toPath());
                    dbManagerStatusDataRepository.executeQueryRaw(sql);
                }
            } catch (IOException | java.net.URISyntaxException e) {
                throw new DbManagerException(DbManagerError.UNABLE_TO_FETCH_SEED_FILES, e.getMessage());
            }
            dbManagerStatusData.setSeedFilesRan(true);
            dbManagerStatusDataRepository.insertOrUpdate(dbManagerStatusData);
        }

        List<String> migrationFileNames = new ArrayList<>();
        try {
            migrationFileNames = getResourceFiles(getMigrationFileResourceDirectory());
        } catch (IOException e) {
            throw new DbManagerException(DbManagerError.UNABLE_TO_FETCH_MIGRATION_FILES,e.getMessage());
        }

        try {
            HashMap<Long, String> migrationNumbersOnlyAndFilenames = new HashMap<>();
            migrationFileNames.forEach(migrationFileName -> migrationNumbersOnlyAndFilenames.put(Long.parseLong(migrationFileName.replaceAll("[^0-9]", "")), migrationFileName));
            List<Long> migrationFilenameNumbersOnly = migrationNumbersOnlyAndFilenames.keySet().stream().sorted().collect(Collectors.toList());

            if(dbManagerStatusData.getFilenameOfLastMigrationFileThatWasRun() != null && !dbManagerStatusData.getFilenameOfLastMigrationFileThatWasRun().isBlank()) {
                migrationFilenameNumbersOnly =
                    migrationFilenameNumbersOnly.subList(
                        migrationFilenameNumbersOnly.indexOf(Long.parseLong(dbManagerStatusData.getFilenameOfLastMigrationFileThatWasRun().replaceAll("[^0-9]", ""))),
                        migrationFilenameNumbersOnly.size()-1
                    );
            }
            for (Long migrationFilenameNumberOnly : migrationFilenameNumbersOnly) {
                String sql = readString(getFileFromResource(migrationNumbersOnlyAndFilenames.get(migrationFilenameNumberOnly)).toPath());
                dbManagerStatusDataRepository.executeQueryRaw(sql);
                dbManagerStatusData.setFilenameOfLastMigrationFileThatWasRun(migrationNumbersOnlyAndFilenames.get(migrationFilenameNumberOnly));
                dbManagerStatusDataRepository.insertOrUpdate(dbManagerStatusData);
            }
        } catch (IOException | java.net.URISyntaxException e) {
            throw new DbManagerException(DbManagerError.UNABLE_TO_FETCH_MIGRATION_FILES,e.getMessage());
        }
    }

    private static File getFileFromResource(String fileName) throws URISyntaxException {
        ClassLoader classLoader = DbManagerUtils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new File(resource.toURI());
        }
    }

    private static List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();
        try (
            InputStream in = getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }
        return filenames;
    }

    private static InputStream getResourceAsStream(String resource) {
        final InputStream in = getContextClassLoader().getResourceAsStream(resource);
        return in == null ? DbManagerUtils.class.getResourceAsStream(resource) : in;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
