extra["POM_NAME"] = "toolkit4j-bom"

extra["POM_DESCRIPTION"] = "Bill of materials for aligning toolkit4j module versions."

dependencies {
  constraints {
    api(projects.libs.collection)
    api(projects.libs.text)
    api(projects.libs.dataModel)
    api(projects.libs.net)
    api(projects.libs.hibernateSnowflakeId)
    api(projects.libs.quartzTask)
  }
}
