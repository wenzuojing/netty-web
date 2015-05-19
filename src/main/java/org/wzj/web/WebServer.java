package org.wzj.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wzj.web.imp.RequestImp;
import org.wzj.web.imp.ResponseImp;
import org.wzj.web.util.Threads;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by wens on 15-5-13.
 */
public class WebServer {

    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    private RouterMap routerMap = new RouterMap();

    private ServerConfig serverConfig;

    public void get(String route, String handle) {
        this.addRoute(route, HttpMethod.GET.name(), handle);
    }

    public void post(String route, String handle) {
        this.addRoute(route, HttpMethod.POST.name(), handle);
    }

    public void put(String route, String handle) {
        this.addRoute(route, HttpMethod.PUT.name(), handle);
    }

    public void delete(String route, String handle) {
        this.addRoute(route, HttpMethod.DELETE.name(), handle);
    }

    public void staticFile(String route) {
        this.addRoute(route, HttpMethod.GET.name(), "org.wzj.web.StaticSource.servingStaticFile");
        this.addRoute(route, HttpMethod.HEAD.name(), "org.wzj.web.StaticSource.servingStaticFile");

    }

    private void addRoute(String route, String method, String handle) {
        routerMap.addRouter(route, method, handle);
    }


    public void run(ServerConfig config) {
        initServer(config);
        try {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1, Threads.makeName(serverConfig.getServerName()));
            EventLoopGroup workerGroup = new NioEventLoopGroup(serverConfig.getMaxConns(), Threads.makeName(String.format("%s-%s", serverConfig.getServerName(), "worker")));

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.option(ChannelOption.SO_BACKLOG, 1024);
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ServerHandlerInitializer(null));

                Channel ch = b.bind(this.serverConfig.getPort()).sync().channel();
                ch.closeFuture().sync();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            throw new WebException("Run server fail.", e);
        }

    }


    private void initServer(ServerConfig config) {
        if (config == null) {
            config = new ServerConfig();
        }

        this.serverConfig = config;
        StaticSource.staticDir = this.serverConfig.getStaticDir();
    }


    private void process(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK);
        httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");

        String method = httpRequest.getMethod().name();

        String uri = httpRequest.getUri();

        RequestImp request = new RequestImp(ctx, httpRequest);
        ResponseImp response = new ResponseImp(ctx, httpResponse);
        WebContext webContext = new WebContext(request, response);

        try {
            if (!routerHandle(method, uri, webContext)) {
                response.setStatus(NOT_FOUND.code(), NOT_FOUND.reasonPhrase());
            }
        } catch (Exception e) {
            log.error("router handle fail.", e);
            response.setStatus(INTERNAL_SERVER_ERROR.code(), INTERNAL_SERVER_ERROR.reasonPhrase());
        }

        if (!response.hasFinish()) {
            response.finish(HttpHeaders.isKeepAlive(httpRequest));
        }

        log.info(request + " " + response);

    }

    private boolean routerHandle(String method, String uri, WebContext webContext) {
        return routerMap.handle(method, uri, webContext);
    }

    public void scanRouters(String packageName) {
        routerMap.scanRouters(packageName);
    }


    private class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

        private final SslContext sslCtx;

        public ServerHandlerInitializer(SslContext sslCtx) {
            this.sslCtx = sslCtx;
        }

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc()));
            }
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(65536));
            //p.addLast(new ChunkedWriteHandler());
            p.addLast(new HttpContentCompressor());
            p.addLast(new ServerHandler());
        }
    }

    private class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

            if (!req.getDecoderResult().isSuccess()) {
                sendError(ctx, BAD_REQUEST);
                return;
            }

            process(ctx, req);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("An exception occurs, close the connect.", cause);
            ctx.close();
        }

        private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
