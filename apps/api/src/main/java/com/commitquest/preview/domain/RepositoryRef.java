package com.commitquest.preview.domain;

public record RepositoryRef(String owner, String name) {

    public String fullName() {
        return owner + "/" + name;
    }

    public String webUrl() {
        return "https://github.com/" + fullName();
    }
}
