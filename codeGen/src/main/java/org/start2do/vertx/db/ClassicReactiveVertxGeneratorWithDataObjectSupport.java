package org.start2do.vertx.db;

import io.github.jklingsporn.vertx.jooq.generate.builder.BuildOptions;
import io.github.jklingsporn.vertx.jooq.generate.builder.DelegatingVertxGenerator;
import io.github.jklingsporn.vertx.jooq.generate.builder.PredefinedNamedInjectionStrategy;
import io.github.jklingsporn.vertx.jooq.generate.builder.VertxGeneratorBuilder;
import java.util.EnumSet;

/**
 * @Author Lijie
 *
 * @date 2021/2/9:11:27
 */
public class ClassicReactiveVertxGeneratorWithDataObjectSupport extends DelegatingVertxGenerator {
  public ClassicReactiveVertxGeneratorWithDataObjectSupport() {
    super(
        VertxGeneratorBuilder.init()
            .withClassicAPI()
            .withPostgresReactiveDriver()
            .withGuice(true, PredefinedNamedInjectionStrategy.DISABLED)
            .build(
                new BuildOptions()
                    .withBuildFlags(
                        EnumSet.of(BuildOptions.BuildFlag.GENERATE_DATA_OBJECT_ANNOTATION))));
  }
}
