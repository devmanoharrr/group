# Data directory

The crowdsourced data microservice writes citizen water quality submissions to the SQLite database `crowdsourced-data.db` in this
folder. The database file is generated automatically the first time the service persists a record. You can explore the contents
with `sqlite3 data/crowdsourced-data.db` once data has been collected.

The JPA layer in the `crowdsourced-data-service` uses `spring.jpa.hibernate.ddl-auto=update`, so additional columns required by new
features will be created automatically when the service starts.
