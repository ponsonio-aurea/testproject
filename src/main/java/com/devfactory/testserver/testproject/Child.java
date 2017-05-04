package com.devfactory.testserver.testproject;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Child implements Identifiable {
    private int id;
    private String name;
    private Parent parent;
}
