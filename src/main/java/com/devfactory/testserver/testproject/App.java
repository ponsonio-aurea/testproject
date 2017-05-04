package com.devfactory.testserver.testproject;

import java.util.Collection;

public class App {
    public static void main(String[] args) {
        Parent p = new Parent();
        p.setName("parent");
        Child c1 = new Child();
        c1.setName("child1");
        c1.setParent(p);

        Child c2 = new Child();
        c2.setName("child2");
        c2.setParent(p);

        Child c3 = new Child();
        c3.setName("child3");
        c3.setParent(p);

        p.getChildren().add(c1);
        p.getChildren().add(c2);
        p.getChildren().add(c3);

        InMemoryRepository.getInstance().save(p);

        Collection<Child>
                child2 =
                InMemoryRepository.getInstance().findAll(Child.class, c -> c.getName().equals("child2"));

        Child found = child2.iterator().next();
        System.out.println(found.getId()+": "+found.getName());
    }
}
