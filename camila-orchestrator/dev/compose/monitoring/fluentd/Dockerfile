FROM fluentd:v1.17.1-1.0

USER root

RUN gem install fluent-plugin-elasticsearch

USER fluent

COPY fluent.conf /fluentd/fluent.conf

CMD ["fluentd", "-c", "/fluentd/fluent.conf", "-v"]
