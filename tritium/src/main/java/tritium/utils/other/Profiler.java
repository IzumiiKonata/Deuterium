package tritium.utils.other;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Profiler {

    ProfileData root = new ProfileData("Root", null);

    ProfileData current = root;

    public void start(String name) {

        if (current == null) {
//            throw new IllegalStateException("current == null!");
            this.reset();
        }

        ProfileData data = new ProfileData(name, current);

        current.subs.add(data);

        current = data;

    }

    public void end() {

        current.usedTime = System.currentTimeMillis() - current.startTime;
        current = current.parent;

    }

    public void reset() {
        root = new ProfileData("Root", null);
        current = root;
    }

    @Getter
    @RequiredArgsConstructor
    private static class ProfileData {

        private final String name;

        private final ProfileData parent;

        private long startTime = System.currentTimeMillis(), usedTime = 0;

        private final List<ProfileData> subs = new ArrayList<>();

        @Override
        public String toString() {
            return this.name;
        }
    }

    @Override
    public String toString() {

        if (root.usedTime == 0)
            root.usedTime = System.currentTimeMillis() - root.startTime;

        return this.extract(root);

    }

    String space = "";

    private String extract(ProfileData data) {

        StringBuilder result = new StringBuilder();

        result.append(data.name).append(" ").append(data.usedTime).append("ms");

//        System.out.println("Name: " + data.name + ", Parent: " + (data.parent == null ? "null" : data.parent.name) + ", Subs: " + Arrays.toString(data.subs.toArray()));

        space += "  ";

        for (ProfileData sub : data.subs) {
            result.append("\n").append(space).append(this.extract(sub));
        }

        space = space.substring(0, space.length() - 2);

        return result.toString();
    }
}
