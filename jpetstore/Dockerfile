#
#    Copyright 2010-2026 the original author or authors.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

FROM eclipse-temurin:21.0.12.1_1-jdk-noble AS build
WORKDIR /build
COPY . .
RUN chmod +x ./mvnw && ./mvnw -B -Dlicense.skip=true clean package

FROM tomcat:9.0.122-jre21-temurin-noble
RUN groupadd --system app && useradd --system --uid 10001 --gid app app \
    && rm -rf "${CATALINA_HOME}/webapps/"* \
    && chown -R app:app "${CATALINA_HOME}"
COPY --from=build --chown=app:app /build/target/jpetstore.war ${CATALINA_HOME}/webapps/jpetstore.war
USER app
EXPOSE 8080
CMD ["catalina.sh", "run"]
