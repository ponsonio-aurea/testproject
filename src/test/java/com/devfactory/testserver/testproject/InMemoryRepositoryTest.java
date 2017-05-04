package com.devfactory.testserver.testproject;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class InMemoryRepositoryTest {
    @Test
    public void testGetInstance() throws Exception {
        assertThat(InMemoryRepository.getInstance(), CoreMatchers.sameInstance(InMemoryRepository.getInstance()));
    }
}
