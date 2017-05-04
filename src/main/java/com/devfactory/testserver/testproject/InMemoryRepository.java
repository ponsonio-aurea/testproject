package com.devfactory.testserver.testproject;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Highly ineffective repo for in memory storing of data
 */
public final class InMemoryRepository {

    private static InMemoryRepository INSTANCE = new InMemoryRepository();

    public static InMemoryRepository getInstance() {
        return INSTANCE;
    }

    private InMemoryRepository() {
    }

    private Map<Class, Map<Integer, Object>> storage = new ConcurrentHashMap<>();

    public <T extends Identifiable> void save(T object) {

        storage.putIfAbsent(object.getClass(), new ConcurrentHashMap<>());

        Map<Integer, Object> integerObjectMap = storage.get(object.getClass());

        if (object.getId() == 0) {
            object.setId(integerObjectMap.keySet().stream().mapToInt(i -> i).max().orElse(0) + 1);
        }

        Object previousObject = integerObjectMap.put(object.getId(), object);
        if (previousObject == object) { // object was stored already - no need to recurse down
            return;
        }

        // new or replaced object. need to store children
        Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> method.getName().startsWith("get")
                        && Identifiable.class.isAssignableFrom(method.getReturnType()))
                .forEach(method -> {
                    try {
                        Identifiable toSave = (Identifiable) method.invoke(object);
                        save(toSave);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new RepositoryException("Exception while storing to repo", ex);
                    }
                });

        Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> method.getName().startsWith("get")
                        && Iterable.class.isAssignableFrom(method.getReturnType()))
                .forEach(method -> {
                    try {
                        Iterable toSave = (Iterable) method.invoke(object);
                        toSave.forEach((elem) -> {
                            this.save((Identifiable) elem);
                        });
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new RepositoryException("Exception while storing to repo", ex);
                    }
                });
    }

    public <T extends Identifiable> T find(Class<T> clz, int id) {
        return (T) storage.getOrDefault(clz, Collections.emptyMap()).get(id);
    }

    public <T extends Identifiable> Collection<T> findAll(Class<T> clz) {
        return getStorageOrEmpty(clz).values().stream().map(obj -> (T) obj)
                .collect(Collectors.toList());
    }

    public <T extends Identifiable> Collection<T> findAll(Class<T> clz, Predicate<? super T> pred) {
        return getStorageOrEmpty(clz).values().stream().map(obj -> (T) obj)
                .filter(pred)
                .collect(Collectors.toList());
    }

    public <T extends Identifiable> void remove(Class<T> clz, int id) {
        storage.getOrDefault(clz, Collections.emptyMap()).remove(id);
    }

    public <T extends Identifiable> void removeIf(Class<T> clz, Predicate<? super T> pred) {
        Map<Integer, Object> storageOrEmpty = getStorageOrEmpty(clz);
        List<Integer> idsToRemove = storageOrEmpty.values().stream()
                .map(obj -> (T) obj).filter(pred)
                .map(toRemove -> toRemove.getId()).collect(Collectors.toList());

        idsToRemove.forEach(storageOrEmpty::remove);
    }

    public <T extends Identifiable> void removeAll(Class<T> clz) {
        getStorageOrEmpty(clz).clear();
    }

    private <T extends Identifiable> Map<Integer, Object> getStorageOrEmpty(Class<T> clz) {
        return storage.getOrDefault(clz, Collections.emptyMap());
    }
}
