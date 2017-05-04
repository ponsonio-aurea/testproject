package com.devfactory.testserver.testproject;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Parent implements Identifiable {
    private int id;
    private String name;
    private List<Child> children = new ArrayList<>();
}
