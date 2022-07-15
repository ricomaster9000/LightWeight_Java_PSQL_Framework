package org.greatgamesonly.shared.opensource.sql.framework.databasesetupmanager.database;


@Entity(tableName = "databasesetupmanager_setup_status_info", repositoryClass = DbManagerStatusDataRepository.class)
class DbManagerStatusData extends BaseEntity {
    @PrimaryKey
    @ColumnName("id")
    protected Long id;
    @ColumnName("seed_files_ran")
    protected Boolean seedFilesRan = false;
    @ColumnName("filename_last_migration_file_successfully_ran")
    protected String filenameOfLastMigrationFileThatWasRun;
    public DbManagerStatusData() {}

    public DbManagerStatusData(Long id, Boolean seedFilesRan, String filenameOfLastMigrationFileThatWasRun) {
        this.seedFilesRan = seedFilesRan;
        this.filenameOfLastMigrationFileThatWasRun = filenameOfLastMigrationFileThatWasRun;
    }

    @Override
    public java.lang.Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSeedFilesRan() {
        return seedFilesRan;
    }

    public void setSeedFilesRan(Boolean seedFilesRan) {
        this.seedFilesRan = seedFilesRan;
    }

    public String getFilenameOfLastMigrationFileThatWasRun() {
        return filenameOfLastMigrationFileThatWasRun;
    }

    public void setFilenameOfLastMigrationFileThatWasRun(String filenameOfLastMigrationFileThatWasRun) {
        this.filenameOfLastMigrationFileThatWasRun = filenameOfLastMigrationFileThatWasRun;
    }
}
