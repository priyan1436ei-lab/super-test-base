package com.example

import com.example.data.model.FoodItem
import com.example.data.network.model.FoodProductDto
import com.example.data.network.model.NutrimentsDto
import com.example.data.network.model.toFoodItem
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun openFoodFactsDto_mapsAccuratelyToDomainEntity() {
    val dto = FoodProductDto(
      code = "3017620422003",
      productName = "Hazelnut Cocoa Spread",
      brands = "Ferrero",
      nutritionGrades = "d",
      nutriments = NutrimentsDto(
        energyKcal100g = 539.0f,
        proteins100g = 6.3f,
        carbs100g = 57.5f,
        fat100g = 30.9f,
        fiber100g = 3.0f
      )
    )

    val domainFood = dto.toFoodItem()

    assertEquals("Hazelnut Cocoa Spread (Ferrero)", domainFood.name)
    assertEquals(539, domainFood.calories)
    assertEquals(6.3f, domainFood.protein, 0.01f)
    assertEquals(57.5f, domainFood.carbs, 0.01f)
    assertEquals(30.9f, domainFood.fat, 0.01f)
    assertEquals(3.0f, domainFood.fibre, 0.01f)
    assertEquals("B", domainFood.healthRating) // 'd' grade mapped
  }

  @Test
  fun fitnessAlarm_formatsTimeCorrectly() {
    val morningAlarm = com.example.data.model.FitnessAlarm(
      alarmId = "test_morning",
      title = "Morning Cardio",
      labelTamil = "காலை உடற்பயிற்சி",
      category = "WORKOUT",
      timeHour = 6,
      timeMinute = 30
    )
    assertEquals("06:30 AM", morningAlarm.getFormattedTime())

    val afternoonAlarm = com.example.data.model.FitnessAlarm(
      alarmId = "test_lunch",
      title = "Lunch",
      category = "MEAL",
      timeHour = 13,
      timeMinute = 15
    )
    assertEquals("01:15 PM", afternoonAlarm.getFormattedTime())

    val midnightAlarm = com.example.data.model.FitnessAlarm(
      alarmId = "test_midnight",
      title = "Night",
      category = "SLEEP",
      timeHour = 0,
      timeMinute = 0
    )
    assertEquals("12:00 AM", midnightAlarm.getFormattedTime())
  }

  @Test
  fun fitnessAlarm_checksActiveDaysCorrectly() {
    val weekdayAlarm = com.example.data.model.FitnessAlarm(
      alarmId = "test_weekdays",
      title = "Work Routine",
      daysOfWeek = "1,2,3,4,5"
    )
    assertTrue(weekdayAlarm.isDayActive(1)) // Monday
    assertTrue(weekdayAlarm.isDayActive(5)) // Friday
    assertFalse(weekdayAlarm.isDayActive(6)) // Saturday
    assertFalse(weekdayAlarm.isDayActive(7)) // Sunday
  }

  @Test
  fun mealReminder_workManagerConstantsAndSchedule() {
    assertEquals("key_meal_type", com.example.data.worker.MealReminderWorker.KEY_MEAL_TYPE)
    assertEquals("fittrack_meal_reminders_channel", com.example.data.worker.MealReminderWorker.CHANNEL_ID)
    assertEquals(8001, com.example.data.worker.MealReminderWorker.NOTIF_ID_BREAKFAST)
    assertEquals(8002, com.example.data.worker.MealReminderWorker.NOTIF_ID_LUNCH)
    assertEquals(8003, com.example.data.worker.MealReminderWorker.NOTIF_ID_DINNER)

    assertEquals(10, com.example.data.worker.MealReminderScheduler.DEFAULT_BREAKFAST_HOUR)
    assertEquals(14, com.example.data.worker.MealReminderScheduler.DEFAULT_LUNCH_HOUR)
    assertEquals(30, com.example.data.worker.MealReminderScheduler.DEFAULT_LUNCH_MIN)
    assertEquals(21, com.example.data.worker.MealReminderScheduler.DEFAULT_DINNER_HOUR)
  }
}

