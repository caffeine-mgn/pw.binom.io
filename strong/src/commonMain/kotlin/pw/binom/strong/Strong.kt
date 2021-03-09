package pw.binom.strong

interface Strong {

    companion object {
        fun config(func: (StrongDefiner) -> Unit) = object : Config {
            override suspend fun apply(strong: StrongDefiner) {
                func(strong)
            }
        }

        suspend fun create(vararg config: Strong.Config): StrongImpl {
            val strong = StrongImpl()
            config.forEach {
                it.apply(strong)
            }

            strong.start()
            return strong
        }
    }

    interface Config {
        suspend fun apply(strong: StrongDefiner)
    }

    interface InitializingBean {
        suspend fun init(strong: Strong)
    }

    interface LinkingBean {
        suspend fun link(strong: Strong)
    }

    interface ServiceProvider {
        suspend fun provide(strong: StrongDefiner)
    }

    interface DestroyableBean {
        suspend fun destroy(strong: Strong)
    }
}