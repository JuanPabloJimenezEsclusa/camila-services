FROM fluentd:v1.16.2-1.1

USER root

RUN gem install fluent-plugin-elasticsearch

USER fluent

COPY fluent.conf /fluentd/fluent.conf

CMD ["fluentd", "-c", "/fluentd/fluent.conf", "-v"]
