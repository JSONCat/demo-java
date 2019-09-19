<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
            <h1>用户列表</h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li class="active"><a href="#"><i class="fa fa-user"></i>用户管理</a></li>
                <li class="active">用户列表</li>
            </ol>
        </section>
        <!-- 内容 -->
        <section class="content container-fluid">
            <!--用户列表-->
            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-header">
                            <div class="wysihtml5-toolbar">
                                <!-- Check all button -->
                                <a href="/user/form" type="button" class="btn btn-success btn-sm"><i class="fa fa-plus"></i>新增</a>&nbsp;
                                <a href="#" type="button" class="btn btn-success btn-sm"><i class="fa fa-trash-o"></i>删除</a>&nbsp;
                                <a href="#" type="button" class="btn btn-success btn-sm"><i class="fa fa-download"></i>导入</a>&nbsp;
                                <a href="#" type="button" class="btn btn-success btn-sm"><i class="fa fa-upload"></i>导出</a>
                            </div>
                            <!-- 工具栏 -->
                            <div class="box-tools">
                                <div class="input-group input-group-sm" style="width: 150px;">
                                    <input type="text" name="table_search" class="form-control pull-right" placeholder="搜索">

                                    <div class="input-group-btn">
                                        <button type="submit" class="btn btn-default"><i class="fa fa-search"></i></button>
                                    </div>
                                </div>
                            </div>

                        </div>
                        <!-- /.box-header -->
                        <div class="box-body table-responsive no-padding">
                            <table class="table table-hover">
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>用户名</th>
                                    <th>手机号</th>
                                    <th>邮箱</th>
                                    <th>更新时间</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${tbUsers}" var="tbUser">
                                <tr>
                                    <td>${tbUser.id}</td>
                                    <td>${tbUser.username}</td>
                                    <td>${tbUser.phone}</td>
                                    <td>${tbUser.email}</td>
                                    <td><fmt:formatDate value="${tbUser.updated}" pattern="yyyy-MM-dd HH:mm:ss"/> </td>
                                    <td>
                                        <a href="www.baidu.com" class="btn btn-default btn-xs"><i class="fa fa-search"></i>查看</a>&nbsp;
                                        <a type="button" class="btn btn-primary btn-xs"><i class="fa fa-edit"></i>编辑</a>&nbsp;
                                        <a type="button" class="btn btn-danger btn-xs"><i class="fa fa-trash-o"></i>删除</a>
                                    </td>
                                </tr>
                                </c:forEach>
                                </tbody></table>
                        </div>
                        <!-- /.box-body -->
                    </div>
                    <!-- /.box -->
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