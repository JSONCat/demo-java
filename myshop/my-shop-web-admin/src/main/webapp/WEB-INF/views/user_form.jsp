<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <title>我的商城  | 用户管理</title>
    <jsp:include page="includes/header.jsp"/>
</head>
<body class="hold-transition skin-blue sidebar-mini">
<div class="wrapper">
    <jsp:include page="includes/nav.jsp"/>
    <jsp:include page="includes/menu.jsp"/>
    <!-- 右侧内容块 -->
    <div class="content-wrapper">
        <!-- 标题和路径 -->
        <section class="content-header">
            <h1>用户管理</h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li class="active"><a href="#"><i class="fa fa-user"></i>用户管理</a></li>
                <li class="active">${tbUser == null ? "新增" : "编辑"}用户</li>
            </ol>
        </section>
        <!-- 内容 -->
        <section class="content container-fluid">
            <div class="row">
                <div class="col-xs-12">
                    <%-- 提交结果 --%>
                    <c:if test="${baseResult != null}">
                        <div class="alert alert-${baseResult.status == 200 ? "success" : "danger"} alert-dismissible">
                            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                                ${baseResult.message}
                        </div>
                    </c:if>
                    <%-- 表单 --%>
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">${tbUser == null ? "新增" : "编辑"}用户</h3>
                        </div>
                        <!-- /.box-header -->
                        <!-- form start -->
                        <form id="inputForm" class="form-horizontal" action="/user/save" method="post" >
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="email" class="col-sm-2 control-label">邮箱</label>
                                    <div class="col-sm-10">
                                        <input type="email" class="form-control" id="email" placeholder="请输入邮箱地址">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="password" class="col-sm-2 control-label">密码</label>

                                    <div class="col-sm-10">
                                        <input type="password" class="form-control" id="password" placeholder="请输入登录密码">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="name" class="col-sm-2 control-label">姓名</label>

                                    <div class="col-sm-10">
                                        <input type="password" class="form-control" id="name" placeholder="请输入用户的姓名">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="phone" class="col-sm-2 control-label">手机</label>

                                    <div class="col-sm-10">
                                        <input type="password" class="form-control" id="phone" placeholder="请输入用户的手机号">
                                    </div>
                                </div>
                            </div>
                            <!-- /.box-body -->
                            <div class="box-footer">
                                <button type="button" class="btn btn-default" onclick="history.go(-1)">返回</button>
                                <button type="submit" class="btn btn-info pull-right">提交</button>
                            </div>
                            <!-- /.box-footer -->
                        </form>
                    </div>
                </div>
            </div>
        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->
    <jsp:include page="includes/copyright.jsp"/>
</div>
<!-- ./wrapper -->
<jsp:include page="includes/footer.jsp"/>
</body>
</html>