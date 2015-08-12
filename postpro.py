import pandas as pd

# Get tt data
tt = pd.DataFrame.from_csv("TT.csv")
tg = pd.DataFrame.from_csv("tags_processed.csv")

# This line gives me all duplicates. take_last is the inverse which means both gives you all rows.
all_duplicates = tt[tt.duplicated(["TrackID","TagID"]) | tt.duplicated(["TrackID","TagID"], take_last=True)]
all_duplicates_t = tg[tg.duplicated(["SongID","TagID"]) | tg.duplicated(["SongID","TagID"], take_last=True)]

# Drop duplicates and keeps for each the first which is always the highest.
max_duplicates = all_duplicates.drop_duplicates(subset=["TrackID","TagID"])
max_duplicates_t = all_duplicates_t.drop_duplicates(subset=["SongID","TagID"])

# Remove all duplicates from tt.
temp = tt.drop(all_duplicates.index)
temp_t = tg.drop(all_duplicates_t.index)

# Combine this with the maximum duplicates gives you a clean set.
clean = pd.concat([temp, max_duplicates])
clean_t = pd.concat([temp_t, max_duplicates_t])

# Export this again.
clean.to_csv("TT_clean.csv")
clean_t.to_csv("tags_processed_clean.csv")
