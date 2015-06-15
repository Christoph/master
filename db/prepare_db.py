import pandas as pd

import database
db = database.InitializeDB()

# Load tracks available in austria
# Dropping duplicate rows
available = pd.read_csv("available_spotify_austria.csv", error_bad_lines=False).drop("ID",1).drop_duplicates()

# Fetch data from db
artist = db.fetchData(db.query_Artist)
track = db.fetchData(db.query_Track)
tt = db.fetchData(db.query_TT)

# Merge artist and track tables 
artist_track = pd.merge(artist, track, left_index=True, right_on="ArtistID", how="right")
artist_track.columns = ["Artist","Name","ArtistID","Listeners","Playcount"]

# Find available tracks
# Mergin the artist_track table with the available tracks
# reset_index() with set index let me keep the index of the left table
# Each row in the Title column with NaN as value does not exist in spotify austria
temp = artist_track.reset_index().merge(available, left_on=["Name","Artist"], right_on=["Title", "Artist"], how="left").set_index("ID")

# Get all the tracks which are not available
remove = temp[~temp["Title"].notnull()].drop("Title",1)

print("Tracks not available in austria")
print(len(remove))

# Remove tracks not available in austria
db.deleteRows(db.Track, remove)

# Find all tracks with less than 6 tags
groups = tt.groupby("TrackID").count().drop("TagID", 1)
remove = groups[groups["Count"]<6]

print("Tracks with less than 6 tags")
print(len(remove))

# Remove tracks with less than 6 tags
db.deleteRows(db.Track, remove)

# Fetch tables
tt = db.fetchData(db.query_TT)
tag = db.fetchData(db.query_Tag)

# Find tags without tracks
# tt right join tag: Each row with NaN in the TrackID column has no track
# Not completly sure
temp = pd.merge(tt, tag, left_on="TagID", right_index=True, how="right").drop(["Count"],1).set_index("TagID")
remove = temp[~temp["TrackID"].notnull()].drop("TrackID",1)

print("Tags without tracks")
print(len(remove))

# Remove tags without tracks
db.deleteRows(db.Tag, remove)

