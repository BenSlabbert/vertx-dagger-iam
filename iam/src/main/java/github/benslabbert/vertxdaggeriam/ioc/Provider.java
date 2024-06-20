/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggeriam.ioc;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import github.benslabbert.vertxdaggercommons.closer.CloserModule;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggeriam.config.ConfigModule;
import github.benslabbert.vertxdaggeriam.repository.RepositoryModule;
import github.benslabbert.vertxdaggeriam.repository.UserRepository;
import github.benslabbert.vertxdaggeriam.service.ServiceModule;
import github.benslabbert.vertxdaggeriam.service.TokenService;
import github.benslabbert.vertxdaggeriam.verticle.ApiVerticle;
import github.benslabbert.vertxdaggerstarter.redis.RedisModule;
import io.vertx.core.Vertx;
import io.vertx.redis.client.RedisAPI;
import jakarta.validation.ValidatorFactory;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      ConfigModule.class,
      CloserModule.class,
      RepositoryModule.class,
      RedisModule.class,
      ServiceModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  ApiVerticle apiVerticle();

  UserRepository userRepository();

  TokenService tokenService();

  ValidatorFactory validatorFactory();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

    @BindsInstance
    Builder httpConfig(Config.HttpConfig httpConfig);

    @BindsInstance
    Builder redisConfig(Config.RedisConfig redisConfig);

    Provider build();
  }

  @Module
  final class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(RedisAPI redisAPI) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
