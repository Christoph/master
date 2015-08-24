import pandas as pd

# CREATE NEW TRACK TABLE
# Change Track table to ID Name number of tags

# Load data
tt = pd.DataFrame.from_csv("TT.csv")
track = pd.DataFrame.from_csv("Track.csv")

# Drop unimportant columns and drop duplicate entries
track = track.drop(["Listeners","Playcount"], axis=1)

# Count the tags per track
counts = tt.drop(["LastFMWeight","Importance"], axis=1).groupby("TrackID").count()

# Create the new table
new_track = pd.merge(track, counts, left_index=True, right_index=True)
new_track.columns = ["Name", "ArtistID", "TagCount"]
new_track.index.names = ["ID"]

# Export 
new_track.to_csv("Track.csv")

# REMOVE TRACKS WITHOUT TT ROWS

minTags = 0

# Load data
tt = pd.DataFrame.from_csv("TT.csv")
track = pd.DataFrame.from_csv("Track.csv")

temp = pd.merge(tt, track, left_on="TrackID", right_index=True)

new_tt = temp.loc[temp["TagCount"]>minTags]
new_track = track.loc[track["TagCount"]>minTags]

# Drop track cols from tt
new_tt = new_tt.drop(track.columns, axis=1)

# Export tables
new_track.to_csv("Track.csv")
new_tt.to_csv("TT.csv")

# SORT ALL TABLES

track = pd.DataFrame.from_csv("Track.csv")
artist = pd.DataFrame.from_csv("Artist.csv", encoding="latin1")
tag = pd.DataFrame.from_csv("Tag.csv")

track.sort(["Name"],inplace=True)
tag.sort(["Name"],inplace=True)
artist.sort(["Name"],inplace=True)

track.to_csv("Track.csv")
tag.to_csv("Tag.csv")
artist.to_csv("Artist.csv")












