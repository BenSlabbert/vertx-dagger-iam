/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.fullstack;

import org.junit.jupiter.api.Test;

class RegisterPageIT extends FullStackTestBase {

  @Test
  void test() {
    navigateTo("/register");
    page.waitForCondition(page.locator("form")::isVisible);
  }
}
