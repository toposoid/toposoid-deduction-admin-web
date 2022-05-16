FROM toposoid/toposoid-core:0.3

WORKDIR /app
ARG TARGET_BRANCH
ENV DEPLOYMENT=local
ENV _JAVA_OPTIONS="-Xms512m -Xmx4g"

RUN git clone https://github.com/toposoid/toposoid-deduction-common.git \
&& cd toposoid-deduction-common \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-deduction-admin-web.git \
&& cd toposoid-deduction-admin-web \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt playUpdateSecret 1> /dev/null \
&& sbt dist \
&& cd /app/toposoid-deduction-admin-web/target/universal \
&& unzip -o toposoid-deduction-admin-web-0.3.zip



COPY ./docker-entrypoint.sh /app/
ENTRYPOINT ["/app/docker-entrypoint.sh"]

