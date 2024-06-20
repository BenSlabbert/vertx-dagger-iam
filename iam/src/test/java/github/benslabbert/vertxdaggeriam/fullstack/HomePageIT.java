/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.fullstack;

import org.junit.jupiter.api.Test;

class HomePageIT extends FullStackTestBase {

  @Test
  void test() {
    navigateTo("/");
    page.waitForCondition(page.getByTestId("home-element")::isVisible);
  }
}
