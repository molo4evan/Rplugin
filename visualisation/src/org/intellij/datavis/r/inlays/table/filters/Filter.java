/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.intellij.datavis.r.inlays.table.filters;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Commodity class implementing the interface {@link
 * IFilter} on a {@link RowFilter}.
 */
abstract public class Filter extends RowFilter implements IFilter {

    /** The set of currently subscribed observers. */
    private Set<IFilterObserver> observers = new HashSet<IFilterObserver>();

    /** The enabled state. */
    private boolean enabled = true;

    /** @see  IFilter#isEnabled() */
    @Override public boolean isEnabled() {
        return enabled;
    }

    /** @see  IFilter#setEnabled(boolean) */
    @Override public void setEnabled(boolean enable) {
        if (enable != this.enabled) {
            this.enabled = enable;
            reportFilterUpdatedToObservers();
        }
    }

    /** @see  IFilter#addFilterObserver(IFilterObserver) */
    @Override public void addFilterObserver(IFilterObserver observer) {
        observers.add(observer);
    }

    /** @see  IFilter#removeFilterObserver(IFilterObserver) */
    @Override public void removeFilterObserver(IFilterObserver observer) {
        observers.remove(observer);
    }

    /** Returns all the registered {@link IFilterObserver} instances. */
    public Set<IFilterObserver> getFilterObservers() {
        return new HashSet<IFilterObserver>(observers);
    }

    /**
     * Method to be called by subclasses to report to the observers that the
     * filter has changed.
     */
    public void reportFilterUpdatedToObservers() {
        for (IFilterObserver obs : new ArrayList<IFilterObserver>(observers)) {
            obs.filterUpdated(this);
        }
    }
}
