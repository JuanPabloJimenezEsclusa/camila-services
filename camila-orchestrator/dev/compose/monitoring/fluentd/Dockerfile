FROM fluent/fluentd:v1.19-debian

ARG FLUENTD_VERSION=1.19

USER root

# https://github.com/uken/fluent-plugin-elasticsearch
RUN gem install fluent-plugin-elasticsearch --no-document --version "~> 6.0.0" \
  && gem sources --clear-all \
  && gem cleanup \
  && mkdir -p /fluentd/buffer \
  && chown -R fluent:fluent /fluentd/buffer

USER fluent

COPY fluent.conf /fluentd/fluent.conf

CMD ["fluentd", "-c", "/fluentd/fluent.conf", "-v"]
