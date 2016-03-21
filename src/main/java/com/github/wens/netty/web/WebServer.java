package com.github.wens.netty.web;

import com.github.wens.netty.web.impl.RequestImp;
import com.github.wens.netty.web.impl.ResponseImp;
import com.github.wens.netty.web.route.RouteMatcher;
import com.github.wens.netty.web.util.Threads;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by wens on 15-5-13.
 */
public class WebServer {

    private static final Logger log = LoggerFactory.getLogger("netty-server");

    private ServerConfig serverConfig;

    private RouteMatcher routeMatcher;
    private ControllerScanner controllerScanner;
    private ControllerInvoker controllerInvoker;


    public WebServer(ServerConfig config) {
        this.serverConfig = config;
        initServer();
        routeMatcher = new RouteMatcher(config.getContextPath());
        controllerScanner = new ControllerScanner(this.routeMatcher, this.serverConfig.getObjectFactory());
        controllerInvoker = new ControllerInvoker(this.routeMatcher, this.serverConfig.getObjectFactory(), this.serverConfig);
    }

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


    private void addRoute(String route, String method, String handle) {
        routeMatcher.addRouter(route, method, handle);
    }


    public void run() {

        try {

            EventLoopGroup bossGroup = new NioEventLoopGroup(1, Threads.makeName(serverConfig.getServerName()));
            EventLoopGroup workerGroup = new NioEventLoopGroup(this.serverConfig.getWorkerThreads(), Threads.makeName(String.format("%s-%s", serverConfig.getServerName(), "worker")));

            try {
                ServerBootstrap b = new ServerBootstrap();
                //b.option(ChannelOption.SO_BACKLOG, 256 );
                //b.option(ChannelOption.SO_RCVBUF, 128);
                //b.option(ChannelOption.SO_SNDBUF, 128);
                b.option(ChannelOption.TCP_NODELAY, true);
                //b.option(ChannelOption.SO_KEEPALIVE, true);
                b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                                //.handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ServerHandlerInitializer(null));

                Channel ch = b.bind(this.serverConfig.getAddr(), this.serverConfig.getPort()).sync().channel();
                log.info("start server on " + this.serverConfig.getAddr() + ":" + this.serverConfig.getPort());
                ch.closeFuture().sync();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            throw new WebException("Run server fail.", e);
        }

    }


    private void initServer() {
        if (this.serverConfig == null) {
            this.serverConfig = new ServerConfig();
        }
    }


    private void process(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {

        HttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_1, OK);
        httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, String.format("text/plain; %s", serverConfig.getCharset()));
        final boolean keepAlive = HttpHeaders.isKeepAlive(httpRequest);
        if (keepAlive) {
            httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }


        final String method = httpRequest.getMethod().name();
        final String uri = httpRequest.getUri();
        final RequestImp request = new RequestImp(ctx, httpRequest);
        final ResponseImp response = new ResponseImp(this.serverConfig.getCharset(), ctx, httpResponse);
        final WebContext webContext = new WebContext(request, response);

        long start = System.currentTimeMillis();
        long end = 0;
        try {
            controllerInvoker.invoke(method, uri, webContext);
        } catch (Exception e) {
            log.error("invoke controller fail.", e);
            response.setStatus(INTERNAL_SERVER_ERROR.code(), INTERNAL_SERVER_ERROR.reasonPhrase());
        } finally {
            end = System.currentTimeMillis();
        }

        if (log.isDebugEnabled()) {
            log.debug("Request:" + request + ",Response:" + response + ",elapse:" + (end - start) + "ms");
        }

        if (!response.hasFinish()) {
            response.finish(keepAlive);
        }


    }


    public void scanRouters(String packageName) {
        controllerScanner.scanControllers(packageName);
    }


    private class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

        private final SslContext sslCtx;

        private EventExecutorGroup executor;

        public ServerHandlerInitializer(SslContext sslCtx) {
            this.sslCtx = sslCtx;
            this.executor = new DefaultEventExecutorGroup(serverConfig.getExecutorThreads(), Threads.makeName("bus-thread"));
        }

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc()));
            }
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(65536));
            //chunked + gzip 貌似某些浏览器解析不了
            //p.addLast(new ChunkedWriteHandler());
            //p.addLast(new HttpContentCompressor());
            p.addLast(this.executor, new ServerHandler());
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
            if (ctx.channel().isActive()) {//try send 500
                ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.close();
            }

        }

        private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, String.format("text/plain; charset=%s", serverConfig.getCharset()));

            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
