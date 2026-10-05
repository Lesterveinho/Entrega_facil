package com.deliverytech.delivery_api.service;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Service;

@Service
public class TracingService {

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private Tracer tracer;

    public void processarPedidoComTracing(String pedidoId) {
        if (tracer == null) {
            // Tracing desabilitado (ex.: perfil de testes) - executa sem spans
            return;
        }
        Span span = tracer.nextSpan()
            .name("processar-pedido")
            .tag("pedido.id", pedidoId)
            .start();

        // No Micrometer Tracing, usamos withSpan(span) em vez de withSpanInScope
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            validarPedidoComSpan(pedidoId);
            calcularFreteComSpan(pedidoId);
            processarPagamentoComSpan(pedidoId);
        } finally {
            span.end();
        }
    }

    private void validarPedidoComSpan(String pedidoId) {
        Span span = tracer.nextSpan()
            .name("validar-pedido")
            .tag("pedido.id", pedidoId)
            .start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            Thread.sleep(50);
            span.tag("validacao.resultado", "sucesso");
        } catch (Exception e) {
            span.tag("error", e.getMessage());
        } finally {
            span.end();
        }
    }

    private void calcularFreteComSpan(String pedidoId) {
        Span span = tracer.nextSpan()
            .name("calcular-frete")
            .tag("pedido.id", pedidoId)
            .start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            Thread.sleep(30);
            span.tag("frete.valor", "15.50");
        } catch (Exception e) {
            span.tag("error", e.getMessage());
        } finally {
            span.end();
        }
    }

    private void processarPagamentoComSpan(String pedidoId) {
        Span span = tracer.nextSpan()
            .name("processar-pagamento")
            .tag("pedido.id", pedidoId)
            .start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            Thread.sleep(100);
            span.tag("pagamento.status", "aprovado");
        } catch (Exception e) {
            span.tag("error", e.getMessage());
        } finally {
            span.end();
        }
    }
}
