const { Builder, By, until } = require("selenium-webdriver");
const chrome = require("selenium-webdriver/chrome");
const { before, describe, beforeEach, afterEach, it } = require("node:test");
let assert;

// TODO: Integrate test module into the rest of the codebase.
// ?? Or, at the very least, document it.

before(async () => {
  ({ assert } = await import("chai"));
});
const loginUrl = describe("Test Selenium", function () {
  this.timeout(180000); // Son hasta tres minutos por test.

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

    assert.isFalse(logoutFound, "message failed");
  });

  it("crear a new task", async () => {
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

    // === Horas Estimadas ===
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

    // === Estado ===
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
    // const stateOption = await driver.findElement(By.css("li[data-value='TODO']"));
    await stateOption.click();

    // === Asignado a ===
    const assignedDropdown = (
      await driver.findElements(By.css("div.MuiAutocomplete-root"))
    )[0];
    await assignedDropdown.click();
    await driver.sleep(1000);
    const assignedTo = await driver.wait(
      // until.elementLocated(By.xpath("//li[contains(., 'David S')]")),
      // until.elementLocated(By.xpath("//li[contains(., 'Victor C')]")),
      // until.elementLocated(By.xpath("//li[contains(., 'Jean ')]")),
      until.elementLocated(By.xpath("//li[contains(., 'Andrés M')]")),
      // until.elementLocated(By.xpath("//li[contains(., 'Luis M')]")),
      // until.elementLocated(By.xpath("//li[contains(., 'Jacob G')]")),
      5000,
    );
    await assignedTo.click();

    // === Sprint ===
    const sprintDropdown = (
      await driver.findElements(By.css("div.MuiAutocomplete-root"))
    )[1];
    await sprintDropdown.click();
    await driver.sleep(1000);
    const sprint = await driver.wait(
      until.elementLocated(By.xpath("//li[contains(., 'Sprint 2')]")),
      // until.elementLocated(By.xpath("//li[contains(., 'Sprint 1')]")),
      // until.elementLocated(By.xpath("//li[contains(., 'Sprint 3')]")),
      5000,
    );
    await sprint.click();

    // === Submit ===
    const submitButton = await driver.findElement(
      By.xpath("//button[contains(., 'Submit')]"),
    );
    await driver.executeScript(
      "arguments[0].removeAttribute('disabled')",
      submitButton,
    );
    await driver.executeScript("arguments[0].click();", submitButton);

    // === Validaciones ===
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

    assert.isTrue(
      found,
      "La tarea 'pruebas selenium' aparece en alguna fila de la tabla",
    );

    const rowText = await matchedRow.getText();
    assert.match(
      rowText,
      /3h|4h/,
      "La fila muestra al menos 3h o 4h como horas estimadas",
    );
  });

  it("completar una task de la lista", async () => {
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

    // Buscar el botón "Mark as Done"
    const allCheckButtons = await driver.findElements(
      By.css("button[aria-label='Mark as Done']"),
    );
    let checkButton = null;

    for (let btn of allCheckButtons) {
      try {
        const parentRow = await btn.findElement(By.xpath("./ancestor::tr"));
        const rowText = await parentRow.getText();
        //buscar el texto en la fila
        if (rowText.toLowerCase().includes("pruebas selenium")) {
          checkButton = btn;
          break;
        }
      } catch {
        continue;
      }
    }

    assert.isNotNull(
      checkButton,
      "Botón de completar tarea encontrado para 'pruebas selenium'",
    );
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

    assert.isTrue(
      updated,
      "La tarea fue actualizada correctamente como Done con 2h reales",
    );
  });
});
