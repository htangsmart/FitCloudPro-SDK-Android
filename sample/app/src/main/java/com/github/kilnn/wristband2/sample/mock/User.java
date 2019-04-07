package com.github.kilnn.wristband2.sample.mock;

public class User {

    private int id;//user id
    private boolean sex;//True for man. False for woman.
    private int age;
    private float height;//User height. Unit cm
    private float weight;//User weight. Unit kg

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
