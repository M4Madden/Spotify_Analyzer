public record PlaylistItem(String id, String name) {
    @Override
    public String toString() {
        return name;
    }
}