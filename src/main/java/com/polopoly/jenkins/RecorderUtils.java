package com.polopoly.jenkins;

import hudson.maven.MavenBuild;

import java.util.List;

public class RecorderUtils {
    public static MavenBuild getLastOrNullIfEmpty(List<MavenBuild> builds) {
        if (builds.isEmpty()) {
                return null;
        } else {
                return builds.get(builds.size() - 1);
        }
    }
}
