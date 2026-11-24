public record TrackItem(String id, String name, String artists, String album,
                        int durationMs, String releaseDate) {
    @Override
    public String toString() {
        return name;   // what the ListView shows
    }
}