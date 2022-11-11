rootProject.name = "accountapp"

// https://github.com/dom4j/dom4j/pull/116#issuecomment-770092526
dependencyResolutionManagement {
    components {
        withModule("org.dom4j:dom4j") {
            allVariants {
                withDependencies {
                    clear()
                }
            }
        }
    }
}
