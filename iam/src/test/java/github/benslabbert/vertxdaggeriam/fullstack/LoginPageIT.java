/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.fullstack;

import org.junit.jupiter.api.Test;

class LoginPageIT extends FullStackTestBase {

  @Test
  void test() {
    navigateTo("/login");
    System.err.printf("html:%n%s%n", page.content());
    page.waitForCondition(page.locator("form")::isVisible);
  }
}
