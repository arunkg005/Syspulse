package com.example.sensys;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SmokeTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testNavigationAndModules() {
        Espresso.onView(ViewMatchers.withText("Sensys"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText("Device Info")).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("Device Insight")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withText("Sensors")).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("Sensor Lab")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withText("Report")).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("Device Health Report")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText("Run Guided Tests")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText("Generate Report")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.pressBack();
    }
}
