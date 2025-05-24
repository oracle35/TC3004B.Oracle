const { Builder, By, until } = require("selenium-webdriver");
const chrome = require("selenium-webdriver/chrome");
const { describe, it, before, beforeEach, afterEach } = require("mocha");
const { assert } = require("chai");

describe("Test Selenium", function () {
  this.timeout(180000); // Hasta tres minutos por test

  let driver;

  beforeEach(async () => {
    driver = await new Builder()
      .forBrowser("chrome")
      .setChromeOptions(new chrome.Options())
      .build();
  });

  afterEach(async () => {
    await driver.quit();
  });

  it("Prueba exitosa de ingreso", async () => {
    await driver.get("http://localhost:8080/login");
    const [usernameField, passwordField] = await driver.findElements(
      By.css("input.MuiInputBase-input"),
    );
    await usernameField.sendKeys("admin");
    await passwordField.sendKeys("admin");

    const loginButton = (await driver.findElements(By.tagName("button")))[0];
    await loginButton.click();

    const logoutBtn = await driver.wait(
      until.elementLocated(
        By.xpath(
          "//button[normalize-space()='LOGOUT' or contains(translate(., 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'LOGOUT')]",
        ),
      ),
      10000,
    );

    assert.isTrue(await logoutBtn.isDisplayed(), "Logout button");
  });

  it("Prueba negativa de ingreso", async () => {
    await driver.get("http://localhost:8080/login");

    const [usernameField, passwordField] = await driver.findElements(
      By.css("input.MuiInputBase-input"),
    );
    await usernameField.sendKeys("admin");
    await passwordField.sendKeys("wrongpassword");

    const loginButton = (await driver.findElements(By.tagName("button")))[0];
    await loginButton.click();

    const errorDiv = await driver.wait(
      until.elementLocated(By.css("div.MuiAlert-message")),
      5000,
    );

    const errorText = await errorDiv.getText();
    assert.include(errorText.toLowerCase(), "invalid username or password");

    let logoutFound = true;
    try {
      await driver.findElement(By.xpath("//button[contains(., 'Logout')]"));
    } catch {
      logoutFound = false;
    }

    assert.isFalse(logoutFound, "Logout button should not appear");
  });

  it("crear una nueva tarea", async () => {
    await driver.get("http://localhost:8080/login");

    const [usernameField, passwordField] = await driver.findElements(
      By.css("input.MuiInputBase-input"),
    );
    await usernameField.sendKeys("admin");
    await passwordField.sendKeys("admin");

    const loginButton = (await driver.findElements(By.tagName("button")))[0];
    await loginButton.click();

    await driver.wait(
      until.elementLocated(
        By.xpath(
          "//button[normalize-space()='LOGOUT' or contains(translate(., 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'LOGOUT')]",
        ),
      ),
      10000,
    );

    const addTaskButton = await driver.wait(
      until.elementLocated(By.xpath("//button[contains(., 'Add Task')]")),
      10000,
    );
    await addTaskButton.click();

    await driver.wait(
      until.elementLocated(By.xpath("//h2[contains(text(), 'Add Task')]")),
      10000,
    );

    const descriptionInput = await driver.findElement(By.name("description"));
    await descriptionInput.sendKeys("pruebas selenium");

    // Slider de horas estimadas
    const sliderThumb = await driver.findElement(
      By.css("span.MuiSlider-thumb"),
    );
    const sliderInput = await driver.findElement(
      By.css("input[name='hoursEstimated']"),
    );
    const actions = driver.actions({ async: true });

    await actions
      .move({ origin: sliderThumb })
      .press()
      .move({ origin: sliderThumb, x: 220, y: 0 })
      .release()
      .perform();

    await driver.sleep(800);
    const finalValue = await sliderInput.getAttribute("value");
    assert.equal(finalValue, "4", "4 horas");

    // Estado
    const stateDropdown = await driver.findElement(
      By.xpath(
        "//label[contains(., 'State')]/following::div[contains(@class,'MuiSelect-select')]",
      ),
    );
    await stateDropdown.click();
    await driver.wait(until.elementLocated(By.css("ul[role='listbox']")), 5000);
    const stateOption = await driver.findElement(
      By.css("li[data-value='IN_PROGRESS']"),
    );
    await stateOption.click();

    // Asignado a
    const assignedDropdown = (
      await driver.findElements(By.css("div.MuiAutocomplete-root"))
    )[0];
    await assignedDropdown.click();
    await driver.sleep(1000);
    const assignedTo = await driver.wait(
      By.xpath("//li[contains(., 'Andrés M')]"),
      5000,
    );
    await assignedTo.click();

    // Sprint
    const sprintDropdown = (
      await driver.findElements(By.css("div.MuiAutocomplete-root"))
    )[1];
    await sprintDropdown.click();
    await driver.sleep(1000);
    const sprint = await driver.wait(
      By.xpath("//li[contains(., 'Sprint 2')]"),
      5000,
    );
    await sprint.click();

    // Submit
    const submitButton = await driver.findElement(
      By.xpath("//button[contains(., 'Submit')]"),
    );
    await driver.executeScript(
      "arguments[0].removeAttribute('disabled')",
      submitButton,
    );
    await driver.executeScript("arguments[0].click();", submitButton);

    await driver.wait(until.elementLocated(By.xpath("//table")), 10000);
    await driver.sleep(1000);

    const rows = await driver.findElements(By.xpath("//table//tr"));
    let found = false;
    let matchedRow = null;

    for (let row of rows) {
      const text = await row.getText();
      if (text.toLowerCase().includes("pruebas selenium")) {
        found = true;
        matchedRow = row;
        break;
      }
    }

    assert.isTrue(found, "La tarea aparece en la tabla");

    const rowText = await matchedRow.getText();
    assert.match(rowText, /3h|4h/, "La fila muestra 3h o 4h");
  });

  it("completar una tarea existente", async () => {
    await driver.get("http://localhost:8080/login");

    const [usernameField, passwordField] = await driver.findElements(
      By.css("input.MuiInputBase-input"),
    );
    await usernameField.sendKeys("admin");
    await passwordField.sendKeys("admin");

    const loginButton = (await driver.findElements(By.tagName("button")))[0];
    await loginButton.click();

    await driver.wait(
      until.elementLocated(
        By.xpath(
          "//button[normalize-space()='LOGOUT' or contains(translate(., 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'LOGOUT')]",
        ),
      ),
      10000,
    );
    await driver.wait(until.elementLocated(By.xpath("//table")), 10000);
    await driver.sleep(1000);

    const allCheckButtons = await driver.findElements(
      By.css("button[aria-label='Mark as Done']"),
    );
    let checkButton = null;

    for (let btn of allCheckButtons) {
      try {
        const parentRow = await btn.findElement(By.xpath("./ancestor::tr"));
        const rowText = await parentRow.getText();
        if (rowText.toLowerCase().includes("pruebas selenium")) {
          checkButton = btn;
          break;
        }
      } catch {
        continue;
      }
    }

    assert.isNotNull(checkButton, "Botón para marcar como done encontrado");
    await checkButton.click();

    await driver.wait(
      until.elementLocated(
        By.xpath("//div[contains(., 'Task Marked as Done')]"),
      ),
      5000,
    );

    const realHoursInput = await driver.findElement(
      By.css("input[type='number']"),
    );
    await realHoursInput.clear();
    await realHoursInput.sendKeys("2");

    const confirmButton = await driver.wait(
      until.elementLocated(
        By.xpath(
          "//button[contains(translate(., 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'CONFIRM')]",
        ),
      ),
      5000,
    );
    await driver.wait(until.elementIsVisible(confirmButton), 5000);
    await confirmButton.click();

    await driver.wait(until.elementLocated(By.xpath("//table")), 10000);
    await driver.sleep(1000);

    const updatedRows = await driver.findElements(By.xpath("//table//tr"));
    let updated = false;

    for (let row of updatedRows) {
      const text = await row.getText();
      if (
        text.toLowerCase().includes("pruebas selenium") &&
        text.toLowerCase().includes("done") &&
        text.toLowerCase().includes("2h")
      ) {
        updated = true;
        break;
      }
    }

    assert.isTrue(updated, "La tarea fue actualizada a 'Done' con 2h");
  });
});
