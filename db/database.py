import pymysql
import pandas as pd
import sqlalchemy as sql


class InitializeDB:
    engine = sql.create_engine('mysql+pymysql://root:@localhost/tags', echo=False)
    
    # Metadata and autoload tables
    metadata = sql.MetaData(bind=engine)

    metadata.reflect()

    # Get table opjects
    Tag = metadata.tables['Tag']
    TT = metadata.tables['TT']
    Track = metadata.tables['Track']
    Artist = metadata.tables['Artist']

    # Querys
    query_Tag = "select * from Tag"
    query_TT = "select * from TT"
    query_Track = "select * from Track"
    query_Artist = "select * from Artist"

    # Fetch table from db into dataframe
    def fetchData(self, query):
        return pd.read_sql_query(query, InitializeDB.engine, index_col = "ID")

    # Update table from pandas dataframe
    def updateTable(self, table, data):
        with InitializeDB.engine.begin() as conn:
            for i,r in data.iterrows():
                conn.execute(table.update()
                        .where(table.c.ID==int(i))
                        .values(r.to_dict()))

    # Insert data into table from pandas dataframe
    def insertTable(self, table, data):
        with InitializeDB.engine.begin() as conn:
            for i,r in data.iterrows():
                conn.execute(table.insert()
                        .values(r.to_dict()))
                    
    # Delete data from table
    def deleteRows(self, table, data):
        with InitializeDB.engine.begin() as conn:
            for i,r in data.iterrows():
                conn.execute(table.delete()
                        .where(table.c.ID==int(i)))
