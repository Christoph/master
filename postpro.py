import pandas as pd

# Get tt data
tt = pd.DataFrame.from_csv("TT.csv")
tp = pd.DataFrame.from_csv("tags_processed.csv")

# Removing duplicate tags from songs

# This line gives me all duplicates. take_last is the inverse which means both gives you all rows.
all_duplicates = tt[tt.duplicated(["TrackID","TagID"]) | tt.duplicated(["TrackID","TagID"], take_last=True)]
all_duplicates_t = tp[tp.duplicated(["SongID","TagID"]) | tp.duplicated(["SongID","TagID"], take_last=True)]

# Drop duplicates and keeps for each the first which is always the highest.
max_duplicates = all_duplicates.drop_duplicates(subset=["TrackID","TagID"])
max_duplicates_t = all_duplicates_t.drop_duplicates(subset=["SongID","TagID"])

# Remove all duplicates from tt.
temp = tt.drop(all_duplicates.index)
temp_t = tp.drop(all_duplicates_t.index)

# Combine this with the maximum duplicates gives you a clean set.
clean = pd.concat([temp, max_duplicates])
clean_t = pd.concat([temp_t, max_duplicates_t])

# Export this again.
clean.to_csv("TT_clean.csv")
clean_t.to_csv("tags_processed_clean.csv")

# Change Track table to ID Name number of tags
tt = pd.DataFrame.from_csv("TT_clean.csv")
track = pd.DataFrame.from_csv("Track.csv")

track = track.drop(["Listeners","Playcount"], axis=1).drop_duplicates()

counts = tt.drop(["LastFMWeight","Importance"], axis=1).groupby("TrackID").count()

new_track = pd.merge(track, counts, left_index=True, right_index=True)
new_track.columns = ["Name", "ArtistID", "TagCount"]

# Export the new Track table
new_track.to_csv("Track_new.csv")
