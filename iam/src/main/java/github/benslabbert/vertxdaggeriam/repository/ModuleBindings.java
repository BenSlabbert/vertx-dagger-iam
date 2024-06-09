/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggeriam.repository;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface ModuleBindings {

  @Binds
  UserRepository redisDB(RedisDB redisDB);

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseable(RedisDB redisDB);
}
